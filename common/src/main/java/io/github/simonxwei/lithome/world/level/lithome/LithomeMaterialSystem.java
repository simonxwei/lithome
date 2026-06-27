package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.material.LithomeMaterialContext;
import io.github.simonxwei.lithome.world.level.lithome.material.LithomeMaterialSampler;
import net.minecraft.core.BlockPos;
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

import java.util.HashMap;
import java.util.Map;

public final class LithomeMaterialSystem {
    private LithomeMaterialSystem() {
    }

    public static void apply(
        final WorldGenRegion region,
        final ChunkAccess chunk,
        final RandomState randomState,
        final BlockState defaultBlock
    ) {
        final Map<Long, LithomeChunkAccess> sourceChunks = new HashMap<>();
        final LithomeManager manager = new LithomeManager(
            (quartX, quartY, quartZ) -> getStoredLithome(
                region,
                sourceChunks,
                quartX,
                quartY,
                quartZ
            ),
            region.getSeed()
        );
        final LithomeMaterialContext materialContext = new LithomeMaterialContext(
            new LithomeMaterialSampler(randomState)
        );
        final ChunkPos chunkPos = chunk.getPos();
        final int minimumBlockX = chunkPos.getMinBlockX();
        final int minimumBlockZ = chunkPos.getMinBlockZ();
        final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        boolean changed = false;

        for (int sectionIndex = 0; sectionIndex < chunk.getSections().length; ++sectionIndex) {
            final LevelChunkSection section = chunk.getSection(sectionIndex);
            if (!section.maybeHas(state -> state == defaultBlock)) {
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
                        for (int localY = 0; localY < 16; ++localY) {
                            final BlockState current = section.getBlockState(
                                localX,
                                localY,
                                localZ
                            );
                            if (current != defaultBlock) {
                                continue;
                            }
                            final int blockY = minimumBlockY + localY;
                            position.set(blockX, blockY, blockZ);
                            final BlockState replacement = manager
                                .getLithome(position)
                                .value()
                                .getMaterials()
                                .resolve(materialContext.at(blockX, blockY, blockZ));
                            if (current != replacement) {
                                section.setBlockState(
                                    localX,
                                    localY,
                                    localZ,
                                    replacement,
                                    false
                                );
                                changed = true;
                            }
                        }
                    }
                }
            } finally {
                section.release();
            }
        }

        if (changed) {
            chunk.markUnsaved();
        }
    }

    private static Holder<Lithome> getStoredLithome(
        final WorldGenRegion region,
        final Map<Long, LithomeChunkAccess> sourceChunks,
        final int quartX,
        final int quartY,
        final int quartZ
    ) {
        final int chunkX = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartX));
        final int chunkZ = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartZ));
        final long chunkKey = ChunkPos.pack(chunkX, chunkZ);
        LithomeChunkAccess lithomeChunk = sourceChunks.get(chunkKey);
        if (lithomeChunk == null) {
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
        return lithomeChunk.getNoiseLithome(quartX, quartY, quartZ);
    }
}
