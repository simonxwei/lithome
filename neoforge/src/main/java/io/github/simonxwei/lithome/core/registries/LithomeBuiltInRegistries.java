package io.github.simonxwei.lithome.core.registries;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeSurfaceRules;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LithomeBuiltInRegistries {

    public static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITION;
    public static final DeferredRegister<MapCodec<? extends SurfaceRules.RuleSource>> MATERIAL_RULES;

    private LithomeBuiltInRegistries() {}

    // public

    public static void init(final IEventBus modBus) {
        // surface rules
        MATERIAL_CONDITION.register(modBus);
        MATERIAL_RULES.register(modBus);

        Constants.LOG.debug("Initialized builtin registries");
    }

    // core

    private static <T> DeferredRegister<T> registerSimple(final Registry<T> registry, final DeferredRegisterBootstrap<T> loader) {
        final DeferredRegister<T> deferredRegister = DeferredRegister.create(registry, Constants.MOD_ID);
        loader.run(deferredRegister);
        return deferredRegister;
    }

    static {
        MATERIAL_CONDITION = registerSimple(BuiltInRegistries.MATERIAL_CONDITION, LithomeSurfaceRules.ConditionSource::bootstrap);
        MATERIAL_RULES = registerSimple(BuiltInRegistries.MATERIAL_RULE, LithomeSurfaceRules.RuleSource::bootstrap);
    }

    // interface

    @FunctionalInterface
    private interface DeferredRegisterBootstrap<T> {

        void run(DeferredRegister<T> deferredRegister);
    }
}
