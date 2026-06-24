package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.data.tags.LithomeBlockTagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class LithomeDataGenerator {

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent.Client event) {
        event.createProvider(LithomeBlockTagsProvider::new);
    }
}
