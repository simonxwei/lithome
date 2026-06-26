package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.Registry;

public final class LithomeSources {

    private LithomeSources() {
    }

    public static MapCodec<? extends LithomeSource> bootstrap(
            final Registry<MapCodec<? extends LithomeSource>> registry
    ) {
        Registry.register(
                registry,
                Constants.id("fixed"),
                FixedLithomeSource.CODEC
        );
        return Registry.register(
                registry,
                Constants.id("multi_noise"),
                MultiNoiseLithomeSource.CODEC
        );
    }
}
