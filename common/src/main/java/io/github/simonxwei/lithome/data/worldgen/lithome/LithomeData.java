package io.github.simonxwei.lithome.data.worldgen.lithome;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.Lithomes;
import net.minecraft.data.worldgen.BootstrapContext;

public final class LithomeData {

    private LithomeData() {
    }

    public static void bootstrap(final BootstrapContext<Lithome> context) {
        context.register(Lithomes.STONE, OverworldLithomes.stone());
        context.register(Lithomes.ANDESITE, OverworldLithomes.andesite());
        context.register(Lithomes.GRANITE, OverworldLithomes.granite());
        context.register(Lithomes.DEEPSLATE, OverworldLithomes.deepslate());
    }
}
