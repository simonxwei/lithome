package io.github.simonxwei.lithome.mixin;

import com.mojang.brigadier.CommandDispatcher;
import io.github.simonxwei.lithome.commands.LithomeCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author simonxwei
 */
@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lithome$init(final Commands.CommandSelection commandSelection, final CommandBuildContext context, final CallbackInfo ci) {
        LithomeCommands.init(this.dispatcher, context);
    }
}
