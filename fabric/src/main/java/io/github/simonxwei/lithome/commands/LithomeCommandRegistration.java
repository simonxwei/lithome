package io.github.simonxwei.lithome.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

/**
 * 主要参考的 Fabric API 与 Minecraft 原版源码类：
 * - net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
 * - net.minecraft.commands.Commands
 *
 * 命令必须在服务端资源及动态注册表完成构建后注册，
 * 不能在 Minecraft 静态 Bootstrap.validate 阶段直接注入 Commands 构造器。
 *
 * @see CommandRegistrationCallback
 * @see Commands
 * @author simonxwei
 */
public final class LithomeCommandRegistration {

    private LithomeCommandRegistration() {}

    // public

    public static void init() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, context, selection) ->
                LithomeCommands.init(dispatcher, context)
        );
    }
}
