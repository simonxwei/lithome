package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.world.level.block.state.BlockState;

public final class Lithome {

    private final BlockState baseRock;

    public Lithome(final BlockState baseRock) {
        this.baseRock = baseRock;
    }

    public BlockState getBaseRock() {
        return this.baseRock;
    }
}
