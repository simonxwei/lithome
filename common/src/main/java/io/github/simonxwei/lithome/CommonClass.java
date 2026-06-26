package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import io.github.simonxwei.lithome.platform.Services;
import io.github.simonxwei.lithome.tags.LithomeBlockTags;

public final class CommonClass {

    private CommonClass() {}

    public static void init() {
        Constants.LOGGER.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        LithomeBuiltInRegistries.bootstrap();
        LithomeBlockTags.init();
    }
}
