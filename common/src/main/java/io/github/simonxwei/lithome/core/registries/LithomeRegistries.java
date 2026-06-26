package io.github.simonxwei.lithome.core.registries;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class LithomeRegistries {

    public static final Identifier ROOT_REGISTRY_NAME;
    public static final ResourceKey<Registry<MapCodec<? extends LithomeSource>>> LITHOME_SOURCE;
    public static final ResourceKey<Registry<Lithome>> LITHOME;

    private LithomeRegistries() {}

    // core

    private static <T> ResourceKey<Registry<T>> createRegistryKey(final String name) {
        return ResourceKey.createRegistryKey(Constants.id(name));
    }

    static {
        ROOT_REGISTRY_NAME = Constants.id("root");
        LITHOME_SOURCE = createRegistryKey("worldgen/lithome_source");
        LITHOME = createRegistryKey("worldgen/lithome");
    }
}
