package io.github.simonxwei.lithome.tags;

import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * @see net.minecraft.tags.BlockTags
 * @author simonxwei
 */
public final class LithomeBlockTags {

    public static final TagKey<Block> BASE_STONE_OVERWORLD;

    private LithomeBlockTags() {}

    // public

    public static void init() {
        Constants.LOGGER.debug("Initialized block tags");
    }

    // core

    private static TagKey<Block> create(final String name) {
        return TagKey.create(Registries.BLOCK, Constants.id(name));
    }

    static {
        BASE_STONE_OVERWORLD = create("base_stone_overworld");
    }
}
