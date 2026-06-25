package io.github.simonxwei.lithome.world.level.levelgen.surface.condition.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record LithomeSteep(int height) implements SurfaceRules.ConditionSource {

    public static final MapCodec<LithomeSteep> CODEC;

    // condition source interface

    @Override
    public MapCodec<LithomeSteep> codec() {
        return CODEC;
    }

    // core

    @Override
    public SurfaceRules.Condition apply(final SurfaceRules.Context context) {
        return new LithomeSteepCondition(context, this.height);
    }

    static {
        CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("height").forGetter(LithomeSteep::height)
        ).apply(instance, LithomeSteep::new));
    }
}
