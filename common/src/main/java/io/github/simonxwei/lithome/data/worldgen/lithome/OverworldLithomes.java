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

    public static Lithome granite() {
        return new Lithome(Blocks.GRANITE.defaultBlockState());
    }

    public static Lithome deepslate() {
        return new Lithome(Blocks.DEEPSLATE.defaultBlockState());
    }

    public static Lithome calcite() {
        return new Lithome(Blocks.CALCITE.defaultBlockState());
    }
}
