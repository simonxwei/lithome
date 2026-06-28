package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;

/**
 * @see net.minecraft.world.level.biome.BiomeResolver
 * @author simonxwei
 */
@FunctionalInterface
public interface LithomeResolver {

    Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ, final LithomeClimateSampler sampler);
}
