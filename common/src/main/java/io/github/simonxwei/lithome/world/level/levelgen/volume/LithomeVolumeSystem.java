package io.github.simonxwei.lithome.world.level.levelgen.volume;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.levelgen.performance.LithomeWorldgenPerformance;
import io.github.simonxwei.lithome.world.level.levelgen.performance.LithomeWorldgenPerformance.VolumeSample;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;

import net.minecraft.server.level.WorldGenRegion;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @see net.minecraft.world.level.levelgen.SurfaceSystem
 * @author simonxwei
 */
public final class LithomeVolumeSystem {
    private LithomeVolumeSystem() {}

    public static void apply(
        final WorldGenRegion region,
        final ChunkAccess chunk,
        final RandomState randomState,
        final WorldGenerationContext generationContext,
        final BlockState defaultBlock,
        final LithomeVolumeRules.RuleSource ruleSource
    ) {
        final ChunkPos chunkPos = chunk.getPos();
        final @Nullable VolumeSample performanceSample =
            LithomeWorldgenPerformance.beginVolume(chunkPos);

        try {
            final Map<Long, LithomeChunkAccess> sourceChunks = new HashMap<>();
            final int minimumWorldBlockY = SectionPos.sectionToBlockCoord(
                chunk.getSectionYFromSectionIndex(0)
            );
            final int maximumWorldBlockYExclusive = SectionPos.sectionToBlockCoord(
                chunk.getSectionYFromSectionIndex(chunk.getSections().length - 1) + 1
            );
            final LithomeManager manager = new LithomeManager(
                (quartX, quartY, quartZ) -> getStoredLithome(
                    region,
                    sourceChunks,
                    performanceSample,
                    quartX,
                    quartY,
                    quartZ
                ),
                region.getSeed()
            ).forVolumeSampling(
                chunkPos.getMinBlockX(),
                minimumWorldBlockY,
                chunkPos.getMinBlockZ(),
                maximumWorldBlockYExclusive
            );
            final LithomeVolumeRules.Context context = new LithomeVolumeRules.Context(
                randomState,
                manager,
                generationContext,
                performanceSample
            );
            final LithomeVolumeRules.VolumeRule runtimeRule = ruleSource.apply(context);

            final int minimumBlockX = chunkPos.getMinBlockX();
            final int minimumBlockZ = chunkPos.getMinBlockZ();
            boolean changed = false;

            for (int sectionIndex = 0; sectionIndex < chunk.getSections().length; ++sectionIndex) {
                final LevelChunkSection section = chunk.getSection(sectionIndex);

                if (performanceSample != null) {
                    performanceSample.incrementSectionsScanned();
                }

                if (!section.maybeHas(state -> state == defaultBlock)) {
                    if (performanceSample != null) {
                        performanceSample.incrementSectionsSkipped();
                    }

                    continue;
                }

                final int minimumBlockY = SectionPos.sectionToBlockCoord(
                    chunk.getSectionYFromSectionIndex(sectionIndex)
                );

                section.acquire();
                try {
                    for (int localX = 0; localX < 16; ++localX) {
                        final int blockX = minimumBlockX + localX;
                        for (int localZ = 0; localZ < 16; ++localZ) {
                            final int blockZ = minimumBlockZ + localZ;
                            context.updateXZ(blockX, blockZ);
                            for (int localY = 0; localY < 16; ++localY) {
                                if (performanceSample != null) {
                                    performanceSample.incrementBlocksChecked();
                                }

                                final BlockState current = section.getBlockState(localX, localY, localZ);
                                if (current != defaultBlock) {
                                    continue;
                                }

                                if (performanceSample != null) {
                                    performanceSample.incrementCandidateBlocks();
                                }

                                final int blockY = minimumBlockY + localY;
                                context.updateY(blockY);
                                final BlockState replacement = runtimeRule.tryApply();

                                if (replacement != null && replacement != current) {
                                    section.setBlockState(localX, localY, localZ, replacement, false);
                                    changed = true;

                                    if (performanceSample != null) {
                                        performanceSample.incrementChangedBlocks();
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    section.release();
                }
            }

            if (performanceSample != null) {
                performanceSample.addVolumeSamplerCells(
                    manager.volumeSamplerCreatedCells()
                );
                performanceSample.addFiddleOffsetComputations(
                    manager.volumeSamplerComputedFiddleOffsets()
                );
            }

            if (changed) {
                chunk.markUnsaved();
            }
        } finally {
            LithomeWorldgenPerformance.finishVolume(performanceSample);
        }
    }

    private static Holder<Lithome> getStoredLithome(
        final WorldGenRegion region,
        final Map<Long, LithomeChunkAccess> sourceChunks,
        final @Nullable VolumeSample performanceSample,
        final int quartX,
        final int quartY,
        final int quartZ
    ) {
        final int chunkX = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartX));
        final int chunkZ = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartZ));
        final long chunkKey = ChunkPos.pack(chunkX, chunkZ);
        LithomeChunkAccess lithomeChunk = sourceChunks.get(chunkKey);

        if (lithomeChunk == null) {
            if (performanceSample != null) {
                performanceSample.incrementSourceChunkCacheMisses();
            }

            final ChunkAccess sourceChunk = region.getChunk(
                chunkX,
                chunkZ,
                ChunkStatus.BIOMES,
                false
            );

            if (sourceChunk == null) {
                throw new IllegalStateException(
                    "Missing BIOMES-stage chunk while reading stored Lithome at "
                        + chunkX + ", " + chunkZ
                );
            }

            if (!(sourceChunk instanceof LithomeChunkAccess storedLithomeChunk)) {
                throw new IllegalStateException(
                    "Chunk does not expose stored Lithome data: " + sourceChunk.getPos()
                );
            }

            lithomeChunk = storedLithomeChunk;
            sourceChunks.put(chunkKey, lithomeChunk);
        }

        if (performanceSample != null) {
            performanceSample.incrementPaletteReads();
        }

        return lithomeChunk.lithome$getNoiseLithome(quartX, quartY, quartZ);
    }
}
