package io.github.simonxwei.lithome.server.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import io.github.simonxwei.lithome.commands.LithomeCommandQueries;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;

/**
 * @see LocateCommand
 * @author simonxwei
 */
public final class LithomeLocateCommand {

    private static final int MAX_LITHOME_SEARCH_RADIUS = 6400;
    private static final int LITHOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
    private static final int LITHOME_SAMPLE_RESOLUTION_VERTICAL = 64;

    private static final DynamicCommandExceptionType ERROR_LITHOME_NOT_FOUND;

    private LithomeLocateCommand() {}

    // public

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        final RequiredArgumentBuilder<CommandSourceStack, ResourceOrTagArgument.Result<Lithome>> target = Commands.argument("lithome", ResourceOrTagArgument.resourceOrTag(context, LithomeRegistries.LITHOME));

        target.executes(c -> locateLithome(c.getSource(), ResourceOrTagArgument.getResourceOrTag(c, "lithome", LithomeRegistries.LITHOME)));

        dispatcher.register(
                Commands.literal("locate")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("lithome")
                                        .then(target)
                        )
        );
    }

    // core

    private static int locateLithome(final CommandSourceStack source, final ResourceOrTagArgument.Result<Lithome> elementOrTag) throws CommandSyntaxException {
        final ServerLevel level = source.getLevel();
        final BlockPos sourcePos = BlockPos.containing(source.getPosition());
        final Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        final Pair<BlockPos, Holder<Lithome>> nearest = LithomeCommandQueries.requireSource(level).findClosestLithome3d(sourcePos, MAX_LITHOME_SEARCH_RADIUS, LITHOME_SAMPLE_RESOLUTION_HORIZONTAL, LITHOME_SAMPLE_RESOLUTION_VERTICAL, elementOrTag, LithomeClimateSampler.create(level.getChunkSource().randomState()), level);
        stopwatch.stop();
        if (nearest == null) {
            throw ERROR_LITHOME_NOT_FOUND.create(elementOrTag.asPrintable());
        }
        return LocateCommand.showLocateResult(source, elementOrTag, sourcePos, nearest, "commands.locate.lithome.success", true, stopwatch.elapsed());
    }

    static {
        ERROR_LITHOME_NOT_FOUND = new DynamicCommandExceptionType(v -> Component.translatableEscape("commands.locate.lithome.not_found", v));
    }
}
