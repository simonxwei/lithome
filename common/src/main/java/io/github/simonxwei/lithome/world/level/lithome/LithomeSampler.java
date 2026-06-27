package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Samples the independent Lithome fields and the shared vanilla
 * continentalness context at quart coordinates.
 */
public final class LithomeSampler {
    private static final double XZ_SCALE = 0.25D;
    private static final double SHIFT_SCALE = 4.0D;

    private final NormalNoise material;
    private final NormalNoise tectonics;
    private final NormalNoise shift;
    private final DensityFunction continentalness;

    private LithomeSampler(
            final NormalNoise material,
            final NormalNoise tectonics,
            final NormalNoise shift,
            final DensityFunction continentalness
    ) {
        this.material = material;
        this.tectonics = tectonics;
        this.shift = shift;
        this.continentalness = continentalness;
    }

    public static LithomeSampler create(
            final RandomState randomState,
            final Climate.Sampler climateSampler
    ) {
        return new LithomeSampler(
                randomState.getOrCreateNoise(LithomeNoises.MATERIAL),
                randomState.getOrCreateNoise(LithomeNoises.TECTONICS),
                randomState.getOrCreateNoise(Noises.SHIFT),
                climateSampler.continentalness()
        );
    }

    public static LithomeSampler create(final RandomState randomState) {
        return create(randomState, randomState.sampler());
    }

    public LithomeClimate.TargetPoint sample(
            final int quartX,
            final int quartY,
            final int quartZ
    ) {
        final int blockX = QuartPos.toBlock(quartX);
        final int blockY = QuartPos.toBlock(quartY);
        final int blockZ = QuartPos.toBlock(quartZ);

        // Matches vanilla shift_a and shift_b, then vanilla shifted_noise.
        final double shiftX = this.shift.getValue(
                blockX * XZ_SCALE,
                0.0D,
                blockZ * XZ_SCALE
        ) * SHIFT_SCALE;
        final double shiftZ = this.shift.getValue(
                blockZ * XZ_SCALE,
                blockX * XZ_SCALE,
                0.0D
        ) * SHIFT_SCALE;

        final double sampleX = blockX * XZ_SCALE + shiftX;
        final double sampleZ = blockZ * XZ_SCALE + shiftZ;

        final float materialValue = (float) this.material.getValue(sampleX, 0.0D, sampleZ);
        final float tectonicsValue = (float) this.tectonics.getValue(sampleX, 0.0D, sampleZ);
        final float continentalnessValue = (float) this.continentalness.compute(
                new DensityFunction.SinglePointContext(blockX, blockY, blockZ)
        );

        return LithomeClimate.target(
                materialValue,
                tectonicsValue,
                continentalnessValue
        );
    }
}
