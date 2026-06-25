package io.github.simonxwei.lithome.core.registries;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class LithomeDataPackRegistries {

    private LithomeDataPackRegistries() {}

    // public

    public static void init() {
        DynamicRegistries.register(LithomeRegistries.LITHOME, Lithome.DIRECT_CODEC);
    }
}
