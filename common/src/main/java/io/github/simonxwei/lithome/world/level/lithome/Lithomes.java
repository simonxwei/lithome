package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.resources.ResourceKey;

public final class Lithomes {
    public static final ResourceKey<Lithome> STONE;
    public static final ResourceKey<Lithome> ANDESITE;
    public static final ResourceKey<Lithome> GRANITE;
    public static final ResourceKey<Lithome> DEEPSLATE;
    public static final ResourceKey<Lithome> CALCITE;

    private Lithomes() {
    }

    private static ResourceKey<Lithome> register(final String name) {
        return ResourceKey.create(LithomeRegistries.LITHOME, Constants.id(name));
    }

    static {
        STONE = register("stone");
        ANDESITE = register("andesite");
        GRANITE = register("granite");
        DEEPSLATE = register("deepslate");
        CALCITE = register("calcite");
    }
}
