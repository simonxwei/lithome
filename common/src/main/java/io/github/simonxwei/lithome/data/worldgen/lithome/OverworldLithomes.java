package io.github.simonxwei.lithome.data.worldgen.lithome;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import net.minecraft.world.level.block.Blocks;

public final class OverworldLithomes {

    private OverworldLithomes() {
    }

    public static Lithome stone() {
        return new Lithome(Blocks.STONE.defaultBlockState());
    }

    public static Lithome andesite() {
        return new Lithome(Blocks.ANDESITE.defaultBlockState());
    }
}
