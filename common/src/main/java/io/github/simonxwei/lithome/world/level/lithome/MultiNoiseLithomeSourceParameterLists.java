package io.github.simonxwei.lithome.world.level.lithome;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.resources.ResourceKey;

public final class MultiNoiseLithomeSourceParameterLists {

    public static final ResourceKey<MultiNoiseLithomeSourceParameterList> OVERWORLD =
            ResourceKey.create(
                    LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST,
                    Constants.id("overworld")
            );

    private MultiNoiseLithomeSourceParameterLists() {
    }
}
