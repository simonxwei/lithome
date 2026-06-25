package io.github.simonxwei.lithome.core.registries;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class LithomeDataPackRegistries {

    private LithomeDataPackRegistries() {}

    @SubscribeEvent
    private static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(LithomeRegistries.LITHOME, Lithome.DIRECT_CODEC);
    }
}
