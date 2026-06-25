package io.github.simonxwei.lithome.world.level.levelgen;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.world.level.levelgen.surface.condition.custom.LithomeSteep;
import io.github.simonxwei.lithome.world.level.levelgen.surface.rule.Skip;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LithomeSurfaceRules {

    private LithomeSurfaceRules() {}

    // public

    public interface ConditionSource {

        static void bootstrap(final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> deferredRegister) {
            register(deferredRegister, "steep", LithomeSteep.CODEC);
        }
    }

    public interface RuleSource {

        static void bootstrap(final DeferredRegister<MapCodec<? extends SurfaceRules.RuleSource>> deferredRegister) {
            register(deferredRegister, "skip", Skip.CODEC);
        }
    }

    // core

    private static <A> void register(final DeferredRegister<MapCodec<? extends A>> deferredRegister, final String name, final MapCodec<? extends A> codec) {
        deferredRegister.register(name, () -> codec);
    }
}
