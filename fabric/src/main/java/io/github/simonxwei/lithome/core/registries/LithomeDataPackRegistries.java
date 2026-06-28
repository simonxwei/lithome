package io.github.simonxwei.lithome.core.registries;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSourceParameterList;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class LithomeDataPackRegistries {

    private LithomeDataPackRegistries() {

    }

    public static void init() {

        // Lithome is referenced by ResourceOrTagArgument in the server command tree,
        // so the client must receive this registry before rebuilding that tree.
        DynamicRegistries.registerSynced(

            LithomeRegistries.LITHOME,

            Lithome.DIRECT_CODEC

        );

        DynamicRegistries.register(

            LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST,
            MultiNoiseLithomeSourceParameterList.DIRECT_CODEC

        );

        DynamicRegistries.register(

            LithomeRegistries.NOISE_SETTINGS,

            LithomeNoiseGeneratorSettings.DIRECT_CODEC

        );

    }

}
