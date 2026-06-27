package io.github.simonxwei.lithome.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSourceProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class LithomeCommandQueries {

    public static final SimpleCommandExceptionType ERROR_UNAVAILABLE =
            new SimpleCommandExceptionType(
                    Component.translatable("commands.lithome.unavailable")
            );
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED =
            new SimpleCommandExceptionType(
                    Component.translatable("argument.pos.unloaded")
            );

    private LithomeCommandQueries() {
    }

    public static LithomeSource requireSource(final ServerLevel level)
            throws CommandSyntaxException {
        final ChunkGenerator generator = level.getChunkSource().getGenerator();
        if (!(generator instanceof LithomeSourceProvider provider)) {
            throw ERROR_UNAVAILABLE.create();
        }
        return provider.lithome$getLithomeSource(level.registryAccess())
                .orElseThrow(ERROR_UNAVAILABLE::create);
    }

    public static StoredResult stored(
            final ServerLevel level,
            final BlockPos position
    ) throws CommandSyntaxException {
        requireSource(level);
        try {
            final LithomeManager manager = new LithomeManager(
                    (quartX, quartY, quartZ) -> getStoredNoiseLithome(
                            level,
                            quartX,
                            quartY,
                            quartZ
                    ),
                    level.getSeed()
            );
            return new StoredResult(position, manager.getLithomeSelection(position));
        } catch (final MissingChunkException exception) {
            throw ERROR_NOT_LOADED.create();
        }
    }

    public static SampleResult sampled(
            final ServerLevel level,
            final BlockPos position
    ) throws CommandSyntaxException {
        final LithomeSource source = requireSource(level);
        final Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        final LithomeManager manager = new LithomeManager(
                (quartX, quartY, quartZ) -> source.getNoiseLithome(
                        quartX,
                        quartY,
                        quartZ,
                        sampler
                ),
                level.getSeed()
        );
        final LithomeManager.Selection selection = manager.getLithomeSelection(position);
        final Climate.TargetPoint climate = sampler.sample(
                selection.quartX(),
                selection.quartY(),
                selection.quartZ()
        );
        return new SampleResult(position, selection, climate);
    }

    private static Holder<Lithome> getStoredNoiseLithome(
            final ServerLevel level,
            final int quartX,
            final int quartY,
            final int quartZ
    ) {
        final int chunkX = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartX));
        final int chunkZ = SectionPos.blockToSectionCoord(QuartPos.toBlock(quartZ));
        final ChunkAccess chunk = level.getChunk(
                chunkX,
                chunkZ,
                ChunkStatus.FULL,
                false
        );
        if (chunk == null) {
            throw new MissingChunkException();
        }
        if (!(chunk instanceof LithomeChunkAccess lithomeChunk)) {
            throw new IllegalStateException(
                    "Chunk does not expose stored Lithome data: " + chunk.getPos()
            );
        }
        return lithomeChunk.getNoiseLithome(quartX, quartY, quartZ);
    }

    public record StoredResult(
            BlockPos position,
            LithomeManager.Selection selection
    ) {
    }

    public record SampleResult(
            BlockPos position,
            LithomeManager.Selection selection,
            Climate.TargetPoint climate
    ) {
    }

    private static final class MissingChunkException extends RuntimeException {
    }
}
