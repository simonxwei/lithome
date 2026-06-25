package io.github.simonxwei.lithome.world.level.levelgen;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.surface.condition.custom.LithomeSteep;import io.github.simonxwei.lithome.world.level.levelgen.surface.rule.Skip;import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class LithomeSurfaceRules {

    private LithomeSurfaceRules() {}

    // public

    public static void init() {
        // condition
        registerCondition("steep", LithomeSteep.CODEC);
        // rule
        registerRule("skip", Skip.CODEC);

        Constants.LOG.debug("Initialized surface rules");
    }

    // core

    private static void registerCondition(final String name, final MapCodec<? extends SurfaceRules.ConditionSource> codec) {
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, Constants.id(name), codec);
    }

    private static void registerRule(final String name, final MapCodec<? extends SurfaceRules.RuleSource> codec) {
        Registry.register(BuiltInRegistries.MATERIAL_RULE, Constants.id(name), codec);
    }
}
