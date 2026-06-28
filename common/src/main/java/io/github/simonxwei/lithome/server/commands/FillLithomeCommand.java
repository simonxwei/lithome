package io.github.simonxwei.lithome.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @see net.minecraft.server.commands.FillBiomeCommand
 * @author simonxwei
 */
public final class FillLithomeCommand {

    public static final SimpleCommandExceptionType ERROR_NOT_LOADED;
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE;

    private FillLithomeCommand() {}

    // public

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        final RequiredArgumentBuilder<CommandSourceStack, Holder.Reference<Lithome>> target = Commands.argument("lithome", ResourceArgument.resource(context, LithomeRegistries.LITHOME));

        target.executes(c -> fill(
                        c.getSource(),
                        BlockPosArgument.getLoadedBlockPos(c, "from"),
                        BlockPosArgument.getLoadedBlockPos(c, "to"),
                        ResourceArgument.getResource(c, "lithome", LithomeRegistries.LITHOME), l -> true)
        );

        target.then(
                Commands.literal("replace")
                        .then(
                                Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(context, LithomeRegistries.LITHOME))
                                        .executes((c) -> fill(
                                                c.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                BlockPosArgument.getLoadedBlockPos(c, "to"),
                                                ResourceArgument.getResource(c, "lithome", LithomeRegistries.LITHOME),
                                                ResourceOrTagArgument.getResourceOrTag(c, "filter", LithomeRegistries.LITHOME))
                                        )
                        )
        );

        dispatcher.register(
                Commands.literal("filllithome")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(
                                                Commands.argument("to", BlockPosArgument.blockPos())
                                                        .then(target)
                                        )
                        )
        );
    }

    public static Either<Integer, CommandSyntaxException> fill(final ServerLevel level, final BlockPos rawFrom, final BlockPos rawTo, final Holder<Lithome> lithome) {
        return fill(level, rawFrom, rawTo, lithome, l -> true, m -> {});
    }

    public static Either<Integer, CommandSyntaxException> fill(final ServerLevel level, final BlockPos rawFrom, final BlockPos rawTo, final Holder<Lithome> lithome, final Predicate<Holder<Lithome>> filter, final Consumer<Supplier<Component>> successMessageConsumer) {
        final BlockPos from = quantize(rawFrom);
        final BlockPos to = quantize(rawTo);
        final BoundingBox region = BoundingBox.fromCorners(from, to);
        final long volume = (long) region.getXSpan() * (long) region.getYSpan() * (long) region.getZSpan();
        final int limit = level.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS);
        if (volume > (long) limit) {
            return Either.right(ERROR_VOLUME_TOO_LARGE.create(limit, volume));
        }

        final List<ChunkAccess> chunks = new ArrayList<>();
        for(int chunkZ = SectionPos.blockToSectionCoord(region.minZ()); chunkZ <= SectionPos.blockToSectionCoord(region.maxZ()); ++chunkZ) {
            for(int chunkX = SectionPos.blockToSectionCoord(region.minX()); chunkX <= SectionPos.blockToSectionCoord(region.maxX()); ++chunkX) {
                final ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    return Either.right(ERROR_NOT_LOADED.create());
                }

                chunks.add(chunk);
            }
        }

        final MutableInt changedCount = new MutableInt(0);
        for(final ChunkAccess chunk : chunks) {
            if (!(chunk instanceof LithomeChunkAccess lithomeChunk)) {
                throw new IllegalStateException("Chunk does not expose stored Lithome data: " + chunk.getPos());
            }

            lithomeChunk.lithome$fillLithomesFromNoise(
                    makeResolver(changedCount, lithomeChunk, region, lithome, filter),
                    LithomeClimateSampler.create(level.getChunkSource().randomState())
            );
            chunk.markUnsaved();
        }

        // 待修改
        level.getChunkSource().chunkMap.resendBiomesForChunks(chunks);
        successMessageConsumer.accept(() -> Component.translatable("commands.filllithome.success.count", changedCount.intValue(), region.minX(), region.minY(), region.minZ(), region.maxX(), region.maxY(), region.maxZ()));
        return Either.left(changedCount.intValue());
    }

    // core

    private static int fill(final CommandSourceStack source, final BlockPos rawFrom, final BlockPos rawTo, final Holder.Reference<Lithome> lithome, final Predicate<Holder<Lithome>> filter) throws CommandSyntaxException {
        final Either<Integer, CommandSyntaxException> result = fill(source.getLevel(), rawFrom, rawTo, lithome, filter, m -> source.sendSuccess(m, true));
        Optional<CommandSyntaxException> exception = result.right();
        if (exception.isPresent()) {
            throw exception.get();
        }
        return result.left().get();
    }

    // custom

    private static int quantize(final int blockCoord) {
        return QuartPos.toBlock(QuartPos.fromBlock(blockCoord));
    }

    private static BlockPos quantize(final BlockPos block) {
        return new BlockPos(quantize(block.getX()), quantize(block.getY()), quantize(block.getZ()));
    }

    private static LithomeResolver makeResolver(final MutableInt count, final LithomeChunkAccess chunk, final BoundingBox region, final Holder<Lithome> toFill, final Predicate<Holder<Lithome>> filter) {
        return (quartX, quartY, quartZ, sampler) -> {
            final int blockX = QuartPos.toBlock(quartX);
            final int blockY = QuartPos.toBlock(quartY);
            final int blockZ = QuartPos.toBlock(quartZ);
            final Holder<Lithome> currentLithome = chunk.lithome$getNoiseLithome(quartX, quartY, quartZ);
            if (region.isInside(blockX, blockY, blockZ) && filter.test(currentLithome)) {
                count.increment();
                return toFill;
            }
            return currentLithome;
        };
    }

    static {
        ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
        ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((max, count) -> Component.translatableEscape("commands.filllithome.toobig", max, count));
    }
}
