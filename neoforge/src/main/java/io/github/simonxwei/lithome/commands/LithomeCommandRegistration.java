package io.github.simonxwei.lithome.commands;

import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 主要参考的 NeoForge API 与 Minecraft 原版源码类：
 * - net.neoforged.neoforge.event.RegisterCommandsEvent
 * - net.minecraft.commands.Commands
 *
 * RegisterCommandsEvent 在 ReloadableServerResources 建立命令树时触发。
 * 此时数据包动态注册表已经进入 CommandBuildContext，
 * ResourceOrTagArgument<Lithome> 才能安全取得 Lithome 注册表。
 *
 * @see RegisterCommandsEvent
 * @see Commands
 * @author simonxwei
 */
public final class LithomeCommandRegistration {

    private LithomeCommandRegistration() {}

    // public

    public static void init() {
        NeoForge.EVENT_BUS.addListener(
            LithomeCommandRegistration::register
        );
    }

    // core

    private static void register(final RegisterCommandsEvent event) {
        LithomeCommands.init(
            event.getDispatcher(),
            event.getBuildContext()
        );
    }
}
