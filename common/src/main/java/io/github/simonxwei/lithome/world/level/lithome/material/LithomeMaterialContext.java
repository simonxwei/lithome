package io.github.simonxwei.lithome.world.level.lithome.material;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class LithomeMaterialContext {
    private final LithomeMaterialSampler sampler;
    private int blockX;
    private int blockY;
    private int blockZ;

    public LithomeMaterialContext(final LithomeMaterialSampler sampler) {
        this.sampler = sampler;
    }

    public LithomeMaterialContext at(
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        return this;
    }

    public double sample(final ResourceKey<NormalNoise.NoiseParameters> noise) {
        return this.sampler.sample(
            noise,
            this.blockX,
            this.blockY,
            this.blockZ
        );
    }
}
