package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.core.registries.LithomeDataPackRegistries;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeSurfaceRules;
import net.fabricmc.api.ModInitializer;

public final class LithomeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        LithomeDataPackRegistries.init();

        LithomeSurfaceRules.init();
        CommonClass.init();

        Constants.LOG.info("Hello Fabric world!");
    }
}
