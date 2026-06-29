package io.github.simonxwei.lithome.core.registries;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSourceParameterList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class LithomeDataPackRegistries {
    private LithomeDataPackRegistries() {}

    @SubscribeEvent
    private static void registerDatapackRegistries(
            final DataPackRegistryEvent.NewRegistry event
    ) {
        // Lithome is referenced by ResourceOrTagArgument in the server command tree,
        // so the client must receive this registry before rebuilding that tree.
        event.dataPackRegistry(
                LithomeRegistries.LITHOME,
                Lithome.DIRECT_CODEC,
                Lithome.DIRECT_CODEC
        );
        event.dataPackRegistry(
                LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST,
                MultiNoiseLithomeSourceParameterList.DIRECT_CODEC
        );
        event.dataPackRegistry(
                LithomeRegistries.VOLUME_RULE,
                LithomeVolumeRules.RuleSource.DIRECT_CODEC
        );
        event.dataPackRegistry(
                LithomeRegistries.NOISE_SETTINGS,
                LithomeNoiseGeneratorSettings.DIRECT_CODEC
        );
    }
}
