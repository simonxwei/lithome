package io.github.simonxwei.lithome.world.level.lithome.material;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.HashMap;
import java.util.Map;

public final class LithomeMaterialSampler {
    private final RandomState randomState;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noises =
        new HashMap<>();

    public LithomeMaterialSampler(final RandomState randomState) {
        this.randomState = randomState;
    }

    public double sample(
        final ResourceKey<NormalNoise.NoiseParameters> noise,
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        return this.noises
            .computeIfAbsent(noise, this.randomState::getOrCreateNoise)
            .getValue(blockX, blockY, blockZ);
    }
}
