package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class LithomeMod {

    public LithomeMod(final IEventBus eventBus) {
        LithomeBuiltInRegistries.init(eventBus);
        CommonClass.init();

        Constants.LOG.info("Hello NeoForge world!");
    }
}
