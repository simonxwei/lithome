package io.github.simonxwei.lithome;

import io.github.simonxwei.lithome.platform.Services;
import io.github.simonxwei.lithome.tags.LithomeBlockTags;

public final class CommonClass {

    private CommonClass() {}

    public static void init() {
        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        LithomeBlockTags.init();
    }
}
