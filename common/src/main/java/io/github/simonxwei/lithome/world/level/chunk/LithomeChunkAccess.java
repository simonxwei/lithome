package io.github.simonxwei.lithome.world.level.chunk;

import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;

/**
 * @author simonxwei
 */
public interface LithomeChunkAccess extends LithomeManager.NoiseLithomeSource {

    void lithome$fillLithomesFromNoise(final LithomeResolver resolver, final LithomeClimateSampler sampler);
}
