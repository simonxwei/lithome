package io.github.simonxwei.lithome.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.server.commands.FillLithomeCommand;
import io.github.simonxwei.lithome.server.commands.LithomeDebugCommand;
import io.github.simonxwei.lithome.server.commands.LithomeExecuteCommand;
import io.github.simonxwei.lithome.server.commands.LithomeLocateCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @see Commands
 * @author simonxwei
 */
public final class LithomeCommands {

    private LithomeCommands() {}

    // public

    public static void init(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        // vanilla
        LithomeExecuteCommand.register(dispatcher, context);
        FillLithomeCommand.register(dispatcher, context);
        LithomeLocateCommand.register(dispatcher, context);
        // custom
        LithomeDebugCommand.register(dispatcher, context);

        Constants.LOGGER.debug("Initialized commands");
    }
}
