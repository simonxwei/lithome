package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.RegistryAccess;

import java.util.Optional;

public interface LithomeSourceProvider {

    Optional<LithomeSource> lithome$getLithomeSource(RegistryAccess registryAccess);
}
