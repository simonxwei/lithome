package io.github.simonxwei.lithome.world.level.chunk;

import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSampler;

public interface LithomeChunkAccess extends LithomeManager.NoiseLithomeSource {
    void lithome$fillLithomesFromNoise(
            LithomeResolver resolver,
            LithomeSampler sampler
    );
}
