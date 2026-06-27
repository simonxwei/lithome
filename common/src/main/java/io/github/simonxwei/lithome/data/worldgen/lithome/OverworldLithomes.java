package io.github.simonxwei.lithome.data.worldgen.lithome;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeNoises;
import io.github.simonxwei.lithome.world.level.lithome.material.InclusionsLithomeMaterial;
import io.github.simonxwei.lithome.world.level.lithome.material.SingleLithomeMaterial;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OverworldLithomes {
    private OverworldLithomes() {
    }

    public static Lithome stone() {
        return new Lithome(new InclusionsLithomeMaterial(
            Blocks.STONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            0.20F,
            LithomeNoises.COARSE_INCLUSIONS
        ));
    }

    public static Lithome andesite() {
        return single(Blocks.ANDESITE.defaultBlockState());
    }

    public static Lithome granite() {
        return single(Blocks.GRANITE.defaultBlockState());
    }

    public static Lithome deepslate() {
        return single(Blocks.DEEPSLATE.defaultBlockState());
    }

    public static Lithome calcite() {
        return single(Blocks.CALCITE.defaultBlockState());
    }

    private static Lithome single(final BlockState state) {
        return new Lithome(new SingleLithomeMaterial(state));
    }
}
