package io.github.simonxwei.lithome.world.level.lithome.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class InclusionsLithomeMaterial implements LithomeMaterialSettings {
    private static final double NORMAL_NOISE_TARGET_DEVIATION = 1.0 / 3.0;
    private static final double LOWER_REGION = 0.02425;
    private static final double UPPER_REGION = 1.0 - LOWER_REGION;

    public static final MapCodec<InclusionsLithomeMaterial> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("primary").forGetter(InclusionsLithomeMaterial::primary),
            BlockState.CODEC.fieldOf("secondary").forGetter(InclusionsLithomeMaterial::secondary),
            Codec.floatRange(0.0F, 1.0F)
                .fieldOf("target_fraction")
                .forGetter(InclusionsLithomeMaterial::targetFraction),
            ResourceKey.codec(Registries.NOISE)
                .fieldOf("noise")
                .forGetter(InclusionsLithomeMaterial::noise)
        ).apply(instance, InclusionsLithomeMaterial::new));

    private final BlockState primary;
    private final BlockState secondary;
    private final float targetFraction;
    private final ResourceKey<NormalNoise.NoiseParameters> noise;
    private final double threshold;

    public InclusionsLithomeMaterial(
        final BlockState primary,
        final BlockState secondary,
        final float targetFraction,
        final ResourceKey<NormalNoise.NoiseParameters> noise
    ) {
        this.primary = primary;
        this.secondary = secondary;
        this.targetFraction = targetFraction;
        this.noise = noise;
        this.threshold = upperTailThreshold(targetFraction);
    }

    public BlockState primary() {
        return this.primary;
    }

    public BlockState secondary() {
        return this.secondary;
    }

    public float targetFraction() {
        return this.targetFraction;
    }

    public ResourceKey<NormalNoise.NoiseParameters> noise() {
        return this.noise;
    }

    @Override
    public MapCodec<? extends LithomeMaterialSettings> codec() {
        return CODEC;
    }

    @Override
    public BlockState baseRock() {
        return this.primary;
    }

    @Override
    public BlockState resolve(final LithomeMaterialContext context) {
        return context.sample(this.noise) >= this.threshold
            ? this.secondary
            : this.primary;
    }

    private static double upperTailThreshold(final float targetFraction) {
        if (targetFraction <= 0.0F) {
            return Double.POSITIVE_INFINITY;
        }
        if (targetFraction >= 1.0F) {
            return Double.NEGATIVE_INFINITY;
        }
        return NORMAL_NOISE_TARGET_DEVIATION
            * inverseStandardNormal(1.0 - targetFraction);
    }

    private static double inverseStandardNormal(final double probability) {
        final double a1 = -3.969683028665376E+01;
        final double a2 = 2.209460984245205E+02;
        final double a3 = -2.759285104469687E+02;
        final double a4 = 1.383577518672690E+02;
        final double a5 = -3.066479806614716E+01;
        final double a6 = 2.506628277459239E+00;

        final double b1 = -5.447609879822406E+01;
        final double b2 = 1.615858368580409E+02;
        final double b3 = -1.556989798598866E+02;
        final double b4 = 6.680131188771972E+01;
        final double b5 = -1.328068155288572E+01;

        final double c1 = -7.784894002430293E-03;
        final double c2 = -3.223964580411365E-01;
        final double c3 = -2.400758277161838E+00;
        final double c4 = -2.549732539343734E+00;
        final double c5 = 4.374664141464968E+00;
        final double c6 = 2.938163982698783E+00;

        final double d1 = 7.784695709041462E-03;
        final double d2 = 3.224671290700398E-01;
        final double d3 = 2.445134137142996E+00;
        final double d4 = 3.754408661907416E+00;

        if (probability < LOWER_REGION) {
            final double q = Math.sqrt(-2.0 * Math.log(probability));
            return (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6)
                / ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0);
        }
        if (probability > UPPER_REGION) {
            final double q = Math.sqrt(-2.0 * Math.log(1.0 - probability));
            return -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6)
                / ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0);
        }

        final double q = probability - 0.5;
        final double r = q * q;
        return (((((a1 * r + a2) * r + a3) * r + a4) * r + a5) * r + a6) * q
            / (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1.0);
    }
}
