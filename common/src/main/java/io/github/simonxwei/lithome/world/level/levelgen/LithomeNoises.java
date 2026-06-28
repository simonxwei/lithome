package io.github.simonxwei.lithome.world.level.levelgen;

import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * @see net.minecraft.world.level.levelgen.Noises
 * @author simonxwei
 */
public final class LithomeNoises {

    public static final ResourceKey<NormalNoise.NoiseParameters> MATERIAL;
    public static final ResourceKey<NormalNoise.NoiseParameters> TECTONICS;
    public static final ResourceKey<NormalNoise.NoiseParameters> COARSE_INCLUSIONS;

    private LithomeNoises() {}

    // core

    private static ResourceKey<NormalNoise.NoiseParameters> createKey(final String name) {
        return ResourceKey.create(Registries.NOISE, Constants.id(name));
    }

    static {
        MATERIAL = createKey("material");
        TECTONICS = createKey("tectonics");
        COARSE_INCLUSIONS = createKey("coarse_inclusions");
    }
}
