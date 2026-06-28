package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

/**
 * @see net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists
 * @author simonxwei
 */
public final class MultiNoiseLithomeSourceParameterLists {

    public static final ResourceKey<MultiNoiseLithomeSourceParameterList> OVERWORLD;

    private MultiNoiseLithomeSourceParameterLists() {}

    // public

    public static void bootstrap(final BootstrapContext<MultiNoiseLithomeSourceParameterList> context) {
        final HolderGetter<Lithome> lithome = context.lookup(LithomeRegistries.LITHOME);
        context.register(OVERWORLD, new MultiNoiseLithomeSourceParameterList(MultiNoiseLithomeSourceParameterList.Preset.OVERWORLD, lithome));
    }

    // core

    private static ResourceKey<MultiNoiseLithomeSourceParameterList> register(final String name) {
        return ResourceKey.create(LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST, Constants.id(name));
    }

    static {
        OVERWORLD = register("overworld");
    }
}
