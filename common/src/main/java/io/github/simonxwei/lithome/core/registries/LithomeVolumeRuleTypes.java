package io.github.simonxwei.lithome.core.registries;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * 无直接对应的 Minecraft 原版类。
 *
 * 相关参考类：
 * - net.minecraft.core.registries.BuiltInRegistries
 * - net.minecraft.world.level.levelgen.SurfaceRules
 *
 * 附属模组应在模组初始化阶段注册自定义体积规则与条件节点类型。
 *
 * @author simonxwei
 */
public final class LithomeVolumeRuleTypes {
    private LithomeVolumeRuleTypes() {}

    // public

    public static <T extends LithomeVolumeRules.RuleSource> MapCodec<T> registerRuleType(
            final Identifier id,
            final MapCodec<T> codec
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");
        return Registry.register(LithomeBuiltInRegistries.MATERIAL_RULE, id, codec);
    }

    public static <T extends LithomeVolumeRules.ConditionSource> MapCodec<T> registerConditionType(
            final Identifier id,
            final MapCodec<T> codec
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");
        return Registry.register(LithomeBuiltInRegistries.MATERIAL_CONDITION, id, codec);
    }
}
