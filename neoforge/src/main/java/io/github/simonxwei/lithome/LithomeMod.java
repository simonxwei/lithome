package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.core.registries.LithomeBuiltInDeferredRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class LithomeMod {

    public LithomeMod(final IEventBus eventBus) {
        LithomeBuiltInDeferredRegistries.init(eventBus);
        CommonClass.init();

        Constants.LOGGER.info("Hello NeoForge world!");
    }
}
