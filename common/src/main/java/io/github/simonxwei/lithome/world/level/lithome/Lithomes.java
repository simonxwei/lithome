package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.resources.ResourceKey;

public final class Lithomes {

    public static final ResourceKey<Lithome> ANDESITE;

    private Lithomes() {}

    // core

    private static ResourceKey<Lithome> register(final String name) {
        return ResourceKey.create(LithomeRegistries.LITHOME, Constants.id(name));
    }

    static {
        ANDESITE = register("andesite");
    }
}
