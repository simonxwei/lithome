package io.github.simonxwei.lithome.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface LithomeResolver {

    default BlockState getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return Blocks.ANDESITE.defaultBlockState();
    }
}
