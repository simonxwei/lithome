package io.github.simonxwei.lithome.command;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimate;
import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSampler;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class LithomeCommands {
    private static final int MAX_LOCATE_RADIUS = 6400;
    private static final int LOCATE_HORIZONTAL_RESOLUTION = 32;
    private static final int LOCATE_VERTICAL_RESOLUTION = 64;

    private static final DynamicCommandExceptionType ERROR_LITHOME_INVALID =
            new DynamicCommandExceptionType(value -> Component.translatableEscape(
                    "commands.locate.lithome.invalid",
                    value
            ));
    private static final DynamicCommandExceptionType ERROR_LITHOME_NOT_FOUND =
            new DynamicCommandExceptionType(value -> Component.translatableEscape(
                    "commands.locate.lithome.not_found",
                    value
            ));
    private static final DynamicCommandExceptionType ERROR_FILL_TARGET_TAG =
            new DynamicCommandExceptionType(value -> Component.translatableEscape(
                    "commands.filllithome.target_tag",
                    value
            ));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE =
            new Dynamic2CommandExceptionType((max, count) -> Component.translatableEscape(
                    "commands.filllithome.toobig",
                    max,
                    count
            ));
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED =
            new SimpleCommandExceptionType(
                    Component.translatable("commands.execute.conditional.fail")
            );

    private LithomeCommands() {
    }

    public static void register(
            final CommandDispatcher<CommandSourceStack> dispatcher,
            final CommandBuildContext context
    ) {
        registerLocate(dispatcher);
        registerFill(dispatcher);
        registerExecute(dispatcher);
        registerDebug(dispatcher);
    }

    private static void registerLocate(
            final CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("locate")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("lithome")
                                .then(Commands.argument(
                                                "lithome",
                                                ResourceOrTagKeyArgument.resourceOrTagKey(
                                                        LithomeRegistries.LITHOME
                                                )
                                        )
                                        .executes(command -> locateLithome(
                                                command.getSource(),
                                                getLithomeResult(command, "lithome")
                                        ))))
        );
    }

    private static void registerFill(
            final CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        final RequiredArgumentBuilder<CommandSourceStack, ResourceOrTagKeyArgument.Result<Lithome>> target =
                Commands.argument(
                        "lithome",
                        ResourceOrTagKeyArgument.resourceOrTagKey(LithomeRegistries.LITHOME)
                );

        target.executes(command -> fill(
                command.getSource(),
                BlockPosArgument.getLoadedBlockPos(command, "from"),
                BlockPosArgument.getLoadedBlockPos(command, "to"),
                getTargetLithome(command, "lithome"),
                lithome -> true
        ));
        target.then(Commands.literal("replace")
                .then(Commands.argument(
                                "filter",
                                ResourceOrTagKeyArgument.resourceOrTagKey(
                                        LithomeRegistries.LITHOME
                                )
                        )
                        .executes(command -> fill(
                                command.getSource(),
                                BlockPosArgument.getLoadedBlockPos(command, "from"),
                                BlockPosArgument.getLoadedBlockPos(command, "to"),
                                getTargetLithome(command, "lithome"),
                                getLithomeResult(command, "filter")
                        ))));

        dispatcher.register(
                Commands.literal("filllithome")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                        .then(target)))
        );
    }

    private static void registerExecute(
            final CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        final CommandNode<CommandSourceStack> execute = dispatcher.getRoot().getChild("execute");
        if (execute == null) {
            throw new IllegalStateException("Vanilla execute command was not registered");
        }

        dispatcher.register(
                Commands.literal("execute")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(createExecuteCondition(execute, true))
                        .then(createExecuteCondition(execute, false))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createExecuteCondition(
            final CommandNode<CommandSourceStack> execute,
            final boolean expected
    ) {
        return Commands.literal(expected ? "if" : "unless")
                .then(Commands.literal("lithome")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(addConditional(
                                        execute,
                                        Commands.argument(
                                                "lithome",
                                                ResourceOrTagKeyArgument.resourceOrTagKey(
                                                        LithomeRegistries.LITHOME
                                                )
                                        ),
                                        expected,
                                        command -> getLithomeResult(command, "lithome")
                                                .test(LithomeCommandQueries.stored(
                                                        command.getSource().getLevel(),
                                                        BlockPosArgument.getLoadedBlockPos(
                                                                command,
                                                                "pos"
                                                        )
                                                ).selection().lithome())
                                ))));
    }

    private static void registerDebug(
            final CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("lithome")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("query")
                                .executes(command -> query(
                                        command.getSource(),
                                        BlockPos.containing(command.getSource().getPosition())
                                ))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(command -> query(
                                                command.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(
                                                        command,
                                                        "pos"
                                                )
                                        ))))
                        .then(Commands.literal("sample")
                                .executes(command -> sample(
                                        command.getSource(),
                                        BlockPos.containing(command.getSource().getPosition())
                                ))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(command -> sample(
                                                command.getSource(),
                                                BlockPosArgument.getBlockPos(command, "pos")
                                        ))))
                        .then(Commands.literal("compare")
                                .executes(command -> compare(
                                        command.getSource(),
                                        BlockPos.containing(command.getSource().getPosition())
                                ))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(command -> compare(
                                                command.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(
                                                        command,
                                                        "pos"
                                                )
                                        ))))
        );
    }

    private static int locateLithome(
            final CommandSourceStack commandSource,
            final ResourceOrTagKeyArgument.Result<Lithome> target
    ) throws CommandSyntaxException {
        final ServerLevel level = commandSource.getLevel();
        validateTarget(level, target);
        final LithomeSource source = LithomeCommandQueries.requireSource(level);
        final BlockPos origin = BlockPos.containing(commandSource.getPosition());
        final Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        final Pair<BlockPos, Holder<Lithome>> nearest = source.findClosestLithome3d(
                origin,
                MAX_LOCATE_RADIUS,
                LOCATE_HORIZONTAL_RESOLUTION,
                LOCATE_VERTICAL_RESOLUTION,
                target,
                LithomeSampler.create(level.getChunkSource().randomState()),
                level
        );
        stopwatch.stop();

        if (nearest == null) {
            throw ERROR_LITHOME_NOT_FOUND.create(target.asPrintable());
        }

        return LocateCommand.showLocateResult(
                commandSource,
                target,
                origin,
                nearest,
                "commands.locate.lithome.success",
                true,
                stopwatch.elapsed()
        );
    }

    private static int fill(
            final CommandSourceStack source,
            final BlockPos rawFrom,
            final BlockPos rawTo,
            final Holder.Reference<Lithome> target,
            final Predicate<Holder<Lithome>> filter
    ) throws CommandSyntaxException {
        final ServerLevel level = source.getLevel();
        LithomeCommandQueries.requireSource(level);
        final Either<Integer, CommandSyntaxException> result = fill(
                level,
                rawFrom,
                rawTo,
                target,
                filter,
                message -> source.sendSuccess(message, true)
        );
        final Optional<CommandSyntaxException> exception = result.right();
        if (exception.isPresent()) {
            throw exception.get();
        }
        return result.left().orElseThrow();
    }

    public static Either<Integer, CommandSyntaxException> fill(
            final ServerLevel level,
            final BlockPos rawFrom,
            final BlockPos rawTo,
            final Holder<Lithome> target,
            final Predicate<Holder<Lithome>> filter,
            final Consumer<Supplier<Component>> successMessageConsumer
    ) {
        final BlockPos from = quantize(rawFrom);
        final BlockPos to = quantize(rawTo);
        final BoundingBox region = BoundingBox.fromCorners(from, to);
        final long volume = (long) region.getXSpan()
                * (long) region.getYSpan()
                * (long) region.getZSpan();
        final int limit = level.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS);
        if (volume > limit) {
            return Either.right(ERROR_VOLUME_TOO_LARGE.create(limit, volume));
        }

        final List<ChunkAccess> chunks = new ArrayList<>();
        for (int chunkZ = SectionPos.blockToSectionCoord(region.minZ());
             chunkZ <= SectionPos.blockToSectionCoord(region.maxZ());
             ++chunkZ) {
            for (int chunkX = SectionPos.blockToSectionCoord(region.minX());
                 chunkX <= SectionPos.blockToSectionCoord(region.maxX());
                 ++chunkX) {
                final ChunkAccess chunk = level.getChunk(
                        chunkX,
                        chunkZ,
                        ChunkStatus.FULL,
                        false
                );
                if (chunk == null) {
                    return Either.right(LithomeCommandQueries.ERROR_NOT_LOADED.create());
                }
                chunks.add(chunk);
            }
        }

        final MutableInt changedCount = new MutableInt(0);
        final LithomeSampler sampler = LithomeSampler.create(
                level.getChunkSource().randomState()
        );
        for (final ChunkAccess chunk : chunks) {
            if (!(chunk instanceof LithomeChunkAccess lithomeChunk)) {
                throw new IllegalStateException(
                        "Chunk does not expose stored Lithome data: " + chunk.getPos()
                );
            }
            lithomeChunk.lithome$fillLithomesFromNoise(
                    makeResolver(
                            changedCount,
                            lithomeChunk,
                            region,
                            target,
                            filter
                    ),
                    sampler
            );
            chunk.markUnsaved();
        }

        successMessageConsumer.accept(() -> Component.translatable(
                "commands.filllithome.success.count",
                changedCount.intValue(),
                region.minX(),
                region.minY(),
                region.minZ(),
                region.maxX(),
                region.maxY(),
                region.maxZ()
        ));
        return Either.left(changedCount.intValue());
    }

    private static LithomeResolver makeResolver(
            final MutableInt count,
            final LithomeChunkAccess chunk,
            final BoundingBox region,
            final Holder<Lithome> target,
            final Predicate<Holder<Lithome>> filter
    ) {
        return (quartX, quartY, quartZ, sampler) -> {
            final int blockX = QuartPos.toBlock(quartX);
            final int blockY = QuartPos.toBlock(quartY);
            final int blockZ = QuartPos.toBlock(quartZ);
            final Holder<Lithome> current = chunk.getNoiseLithome(
                    quartX,
                    quartY,
                    quartZ
            );
            if (region.isInside(blockX, blockY, blockZ)
                    && filter.test(current)
                    && !current.equals(target)) {
                count.increment();
                return target;
            }
            return current;
        };
    }

    private static int query(
            final CommandSourceStack source,
            final BlockPos position
    ) throws CommandSyntaxException {
        final LithomeCommandQueries.StoredResult result =
                LithomeCommandQueries.stored(source.getLevel(), position);
        final LithomeManager.Selection selection = result.selection();
        source.sendSuccess(() -> Component.translatable(
                "commands.lithome.query.success",
                coordinates(position),
                selection.lithome().getRegisteredName(),
                BuiltInRegistries.BLOCK.getKey(
                        selection.lithome().value().getBaseRock().getBlock()
                ).toString(),
                selection.quartX(),
                selection.quartY(),
                selection.quartZ()
        ), false);
        return 1;
    }

    private static int sample(
            final CommandSourceStack source,
            final BlockPos position
    ) throws CommandSyntaxException {
        final LithomeCommandQueries.SampleResult result =
                LithomeCommandQueries.sampled(source.getLevel(), position);
        final LithomeClimate.TargetPoint parameters = result.parameters();
        final LithomeManager.Selection selection = result.selection();
        source.sendSuccess(() -> Component.translatable(
                "commands.lithome.sample.success",
                coordinates(position),
                selection.lithome().getRegisteredName(),
                formatParameter(parameters.material()),
                formatParameter(parameters.tectonics()),
                formatParameter(parameters.continentalness()),
                selection.quartX(),
                selection.quartY(),
                selection.quartZ()
        ), false);
        return 1;
    }

    private static int compare(
            final CommandSourceStack source,
            final BlockPos position
    ) throws CommandSyntaxException {
        final LithomeCommandQueries.StoredResult stored =
                LithomeCommandQueries.stored(source.getLevel(), position);
        final LithomeCommandQueries.SampleResult sampled =
                LithomeCommandQueries.sampled(source.getLevel(), position);
        final boolean matches = stored.selection().lithome()
                .equals(sampled.selection().lithome());
        source.sendSuccess(() -> Component.translatable(
                "commands.lithome.compare.success",
                coordinates(position),
                stored.selection().lithome().getRegisteredName(),
                sampled.selection().lithome().getRegisteredName(),
                matches
        ), false);
        return matches ? 1 : 0;
    }

    private static Component coordinates(final BlockPos position) {
        return Component.translatable(
                "chat.coordinates",
                position.getX(),
                position.getY(),
                position.getZ()
        );
    }

    private static String formatParameter(final long value) {
        return String.format(
                Locale.ROOT,
                "%.3f",
                Climate.unquantizeCoord(value)
        );
    }

    private static int quantize(final int blockCoordinate) {
        return QuartPos.toBlock(QuartPos.fromBlock(blockCoordinate));
    }

    private static BlockPos quantize(final BlockPos position) {
        return new BlockPos(
                quantize(position.getX()),
                quantize(position.getY()),
                quantize(position.getZ())
        );
    }

    private static ResourceOrTagKeyArgument.Result<Lithome> getLithomeResult(
            final CommandContext<CommandSourceStack> context,
            final String name
    ) throws CommandSyntaxException {
        return ResourceOrTagKeyArgument.getResourceOrTagKey(
                context,
                name,
                LithomeRegistries.LITHOME,
                ERROR_LITHOME_INVALID
        );
    }

    private static Holder.Reference<Lithome> getTargetLithome(
            final CommandContext<CommandSourceStack> context,
            final String name
    ) throws CommandSyntaxException {
        final ResourceOrTagKeyArgument.Result<Lithome> result =
                getLithomeResult(context, name);
        final Optional<net.minecraft.resources.ResourceKey<Lithome>> key =
                result.unwrap().left();
        if (key.isEmpty()) {
            throw ERROR_FILL_TARGET_TAG.create(result.asPrintable());
        }
        return context.getSource()
                .getLevel()
                .registryAccess()
                .lookupOrThrow(LithomeRegistries.LITHOME)
                .get(key.get())
                .orElseThrow(() -> ERROR_LITHOME_INVALID.create(
                        result.asPrintable()
                ));
    }

    private static void validateTarget(
            final ServerLevel level,
            final ResourceOrTagKeyArgument.Result<Lithome> target
    ) throws CommandSyntaxException {
        final Registry<Lithome> registry = level.registryAccess()
                .lookupOrThrow(LithomeRegistries.LITHOME);
        final boolean exists = target.unwrap().map(
                key -> registry.get(key).isPresent(),
                tag -> registry.get(tag).isPresent()
        );
        if (!exists) {
            throw ERROR_LITHOME_INVALID.create(target.asPrintable());
        }
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
            final CommandNode<CommandSourceStack> root,
            final ArgumentBuilder<CommandSourceStack, ?> argument,
            final boolean expected,
            final CommandPredicate predicate
    ) {
        return argument
                .fork(root, command -> expect(
                        command,
                        expected,
                        predicate.test(command)
                ))
                .executes(command -> {
                    if (expected == predicate.test(command)) {
                        command.getSource().sendSuccess(
                                () -> Component.translatable(
                                        "commands.execute.conditional.pass"
                                ),
                                false
                        );
                        return 1;
                    }
                    throw ERROR_CONDITIONAL_FAILED.create();
                });
    }

    private static Collection<CommandSourceStack> expect(
            final CommandContext<CommandSourceStack> context,
            final boolean expected,
            final boolean result
    ) {
        return result == expected
                ? Collections.singleton(context.getSource())
                : Collections.emptyList();
    }

    @FunctionalInterface
    private interface CommandPredicate {
        boolean test(CommandContext<CommandSourceStack> context)
                throws CommandSyntaxException;
    }
}
