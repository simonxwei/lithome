package io.github.simonxwei.lithome.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.simonxwei.lithome.commands.LithomeCommandQueries;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimate;
import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Climate;

import java.util.Locale;

/**
 * @author simonxwei
 */
public final class LithomeDebugCommand {

    private LithomeDebugCommand() {}

    // public

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("lithome")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("query")
                                        .executes(c -> query(c.getSource(), BlockPos.containing(c.getSource().getPosition())))
                                        .then(
                                                Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(c -> query(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos")))
                                        )
                        ).then(
                                Commands.literal("sample")
                                        .executes(c -> sample(c.getSource(), BlockPos.containing(c.getSource().getPosition())))
                                        .then(
                                                Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(c -> sample(c.getSource(), BlockPosArgument.getBlockPos(c, "pos")))
                                        )
                        )
                        .then(
                                Commands.literal("compare")
                                        .executes(c -> compare(c.getSource(), BlockPos.containing(c.getSource().getPosition())))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(c -> compare(
                                                c.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(c, "pos")
                                        )))
                        )
        );
    }

    // core

    private static int query(final CommandSourceStack source, final BlockPos sourcePos) throws CommandSyntaxException {
        final LithomeCommandQueries.StoredResult result = LithomeCommandQueries.stored(source.getLevel(), sourcePos);
        final LithomeManager.Selection selection = result.selection();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.lithome.query.success",
                        coordinates(sourcePos),
                        selection.lithome().getRegisteredName(),
                        BuiltInRegistries.BLOCK.getKey(selection.lithome().value().getDefaultBlock().getBlock()).toString(),
                        selection.quartX(),
                        selection.quartY(),
                        selection.quartZ()
                ),
                false
        );
        return 1;
    }

    private static int sample(final CommandSourceStack source, final BlockPos sourcePos) throws CommandSyntaxException {
        final LithomeCommandQueries.SampleResult result = LithomeCommandQueries.sampled(source.getLevel(), sourcePos);
        final LithomeClimate.TargetPoint parameters = result.parameters();
        final LithomeManager.Selection selection = result.selection();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.lithome.sample.success",
                        coordinates(sourcePos),
                        selection.lithome().getRegisteredName(),
                        formatParameter(parameters.material()),
                        formatParameter(parameters.tectonics()),
                        formatParameter(parameters.continentalness()),
                        selection.quartX(),
                        selection.quartY(),
                        selection.quartZ()
                ),
                false
        );
        return 1;
    }

    private static int compare(final CommandSourceStack source, final BlockPos sourcePos) throws CommandSyntaxException {
        final LithomeCommandQueries.StoredResult stored = LithomeCommandQueries.stored(source.getLevel(), sourcePos);
        final LithomeCommandQueries.SampleResult sampled = LithomeCommandQueries.sampled(source.getLevel(), sourcePos);
        final boolean matches = stored.selection().lithome().equals(sampled.selection().lithome());
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.lithome.compare.success",
                        coordinates(sourcePos),
                        stored.selection().lithome().getRegisteredName(),
                        sampled.selection().lithome().getRegisteredName(),
                        matches
                ),
                false
        );
        return matches ? 1 : 0;
    }

    // custom

    private static Component coordinates(final BlockPos sourcePos) {
        return Component.translatable("chat.coordinates", sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
    }

    private static String formatParameter(final long value) {
        return String.format(Locale.ROOT, "%.3f", Climate.unquantizeCoord(value));
    }
}
