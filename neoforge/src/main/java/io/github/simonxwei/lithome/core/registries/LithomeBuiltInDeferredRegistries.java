package io.github.simonxwei.lithome.core.registries;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkGenerators;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeSurfaceRules;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LithomeBuiltInDeferredRegistries {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS;
    public static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITION;
    public static final DeferredRegister<MapCodec<? extends SurfaceRules.RuleSource>> MATERIAL_RULES;

    private LithomeBuiltInDeferredRegistries() {}

    // public

    public static void init(final IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);

        // surface rules

        MATERIAL_CONDITION.register(modBus);
        MATERIAL_RULES.register(modBus);

        Constants.LOGGER.debug("Initialized builtin registries");
    }

    // core

    private static <T> DeferredRegister<T> registerSimple(
        final Registry<T> registry,
        final DeferredRegisterBootstrap<T> loader
    ) {
        final DeferredRegister<T> deferredRegister = DeferredRegister.create(registry, Constants.MOD_ID);
        loader.run(deferredRegister);
        return deferredRegister;
    }

    static {
        CHUNK_GENERATORS = registerSimple(
            BuiltInRegistries.CHUNK_GENERATOR,
            deferredRegister -> deferredRegister.register("noise", () -> LithomeChunkGenerators.CODEC)
        );

        MATERIAL_CONDITION = registerSimple(
            BuiltInRegistries.MATERIAL_CONDITION,
            LithomeSurfaceRules.ConditionSource::bootstrap
        );

        MATERIAL_RULES = registerSimple(
            BuiltInRegistries.MATERIAL_RULE,
            LithomeSurfaceRules.RuleSource::bootstrap
        );
    }

    // interface

    @FunctionalInterface
    private interface DeferredRegisterBootstrap<T> {

        void run(DeferredRegister<T> deferredRegister);
    }
}
