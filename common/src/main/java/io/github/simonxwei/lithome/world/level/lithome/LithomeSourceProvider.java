package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.RegistryAccess;

import java.util.Optional;

/**
 * @see io.github.simonxwei.lithome.mixin.NoiseBasedChunkGeneratorMixin
 * @author simonxwei
 */
public interface LithomeSourceProvider {

    Optional<LithomeSource> lithome$getLithomeSource(final RegistryAccess registryAccess);
}
