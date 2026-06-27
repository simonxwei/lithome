package io.github.simonxwei.lithome.world.level.lithome.material;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.Registry;

public final class LithomeMaterialSettingsTypes {
    private LithomeMaterialSettingsTypes() {
    }

    public static MapCodec<? extends LithomeMaterialSettings> bootstrap(
        final Registry<MapCodec<? extends LithomeMaterialSettings>> registry
    ) {
        Registry.register(
            registry,
            Constants.id("single"),
            SingleLithomeMaterial.CODEC
        );
        return Registry.register(
            registry,
            Constants.id("inclusions"),
            InclusionsLithomeMaterial.CODEC
        );
    }
}
