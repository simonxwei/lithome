package io.github.simonxwei.lithome.server.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.simonxwei.lithome.commands.LithomeCommandQueries;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

import java.util.Collection;
import java.util.Collections;

/**
 * @see net.minecraft.server.commands.ExecuteCommand
 * @author simonxwei
 */
public final class LithomeExecuteCommand {

    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED;
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT;

    private LithomeExecuteCommand() {}

    // public

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        final LiteralCommandNode<CommandSourceStack> execute = dispatcher.register(Commands.literal("execute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)));

        dispatcher.register(
                Commands.literal("execute")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(addConditionals(execute, Commands.literal("if"), true, context))
                        .then(addConditionals(execute, Commands.literal("unless"), false, context))
        );
    }

    // core

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(final CommandNode<CommandSourceStack> execute, final LiteralArgumentBuilder<CommandSourceStack> parent, final boolean expected, final CommandBuildContext context) {
        final ArgumentBuilder<CommandSourceStack, ?> target = Commands.argument("pos", BlockPosArgument.blockPos());

        target.then(
                addConditional(
                        execute,
                        Commands.argument("lithome", ResourceOrTagArgument.resourceOrTag(context, LithomeRegistries.LITHOME)),
                        expected,
                        c -> ResourceOrTagArgument
                                .getResourceOrTag(c, "lithome", LithomeRegistries.LITHOME)
                                .test(
                                        LithomeCommandQueries.stored(
                                                c.getSource().getLevel(),
                                                BlockPosArgument.getLoadedBlockPos(c, "pos")
                                        ).selection().lithome()
                                )
                )
        );

        parent.then(
                Commands.literal("lithome").then(target)
        );

        for(final DataCommands.DataProvider provider : DataCommands.SOURCE_PROVIDERS) {
            parent.then(
                    provider.wrap(
                            Commands.literal("data"),
                            p -> p.then(
                                    Commands.argument("path", NbtPathArgument.nbtPath())
                                            .fork(
                                                    execute,
                                                    c -> expect(
                                                            c,
                                                            expected,
                                                            checkMatchingData(
                                                                    provider.access(c),
                                                                    NbtPathArgument.getPath(c, "path")
                                                            ) > 0
                                                    )
                                            )
                                            .executes(
                                                    createNumericConditionalHandler(
                                                            expected,
                                                            c -> checkMatchingData(
                                                                    provider.access(c),
                                                                    NbtPathArgument.getPath(c, "path")
                                                            )
                                                    )
                                            )
                            )
                    )
            );
        }

        return parent;
    }

    // custom

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(final CommandNode<CommandSourceStack> root, final ArgumentBuilder<CommandSourceStack, ?> argument, final boolean expected, final CommandPredicate predicate) {
        final ArgumentBuilder<CommandSourceStack, ?> target = argument.fork(root, c -> expect(c, expected, predicate.test(c)));

        return target.executes(c -> {
            if (expected == predicate.test(c)) {
                c.getSource().sendSuccess(() -> Component.translatable(
                                        "commands.execute.conditional.pass"
                                ), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED.create();
        });
    }

    private static Collection<CommandSourceStack> expect(final CommandContext<CommandSourceStack> context, final boolean expected, final boolean result) {
        return result == expected ? Collections.singleton(context.getSource()) : Collections.emptyList();
    }

    private static int checkMatchingData(final DataAccessor accessor, final NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return path.countMatching(accessor.getData());
    }

    private static Command<CommandSourceStack> createNumericConditionalHandler(final boolean expected, final CommandNumericPredicate condition) {
        return expected ? c -> {
            final int count = condition.test(c);
            if (count > 0) {
                c.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", new Object[]{count}), false);
                return count;
            }
            throw ERROR_CONDITIONAL_FAILED.create();
        } : c -> {
            final int count = condition.test(c);
            if (count == 0) {
                c.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(count);
        };
    }

    static {
        ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));
        ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(c -> Component.translatableEscape("commands.execute.conditional.fail_count", c));
    }

    @FunctionalInterface
    private interface CommandNumericPredicate {
        int test(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface CommandPredicate {
        boolean test(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
    }
}
