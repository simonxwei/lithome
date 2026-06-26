package io.github.simonxwei.lithome.world.level.chunk;

import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import net.minecraft.world.level.biome.Climate;

public interface LithomeChunkAccess extends LithomeManager.NoiseLithomeSource {

    void lithome$fillLithomesFromNoise(LithomeResolver resolver, Climate.Sampler sampler);
}
