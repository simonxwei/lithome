package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class LithomeNoises {
    public static final ResourceKey<NormalNoise.NoiseParameters> MATERIAL = createKey("material");
    public static final ResourceKey<NormalNoise.NoiseParameters> TECTONICS = createKey("tectonics");
    public static final ResourceKey<NormalNoise.NoiseParameters> COARSE_INCLUSIONS =
        createKey("coarse_inclusions");

    private LithomeNoises() {
    }

    private static ResourceKey<NormalNoise.NoiseParameters> createKey(final String name) {
        return ResourceKey.create(Registries.NOISE, Constants.id(name));
    }
}
