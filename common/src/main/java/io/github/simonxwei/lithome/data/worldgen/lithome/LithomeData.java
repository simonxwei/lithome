package io.github.simonxwei.lithome.data.worldgen.lithome;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.Lithomes;
import net.minecraft.data.worldgen.BootstrapContext;

public final class LithomeData {

    private LithomeData() {}

    // public

    public static void bootstrap(final BootstrapContext<Lithome> context) {
        context.register(Lithomes.ANDESITE, OverworldLithomes.andesite());
    }
}
