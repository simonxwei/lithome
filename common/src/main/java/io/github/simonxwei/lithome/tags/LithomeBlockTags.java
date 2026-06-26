package io.github.simonxwei.lithome.tags;

import io.github.simonxwei.lithome.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class LithomeBlockTags {

    public static final TagKey<Block> REPLACEABLE_BASE_ROCKS;
    public static final TagKey<Block> BASE_STONE_OVERWORLD;

    private LithomeBlockTags() {
    }

    public static void init() {
        Constants.LOGGER.debug("Initialized block tags");
    }

    private static TagKey<Block> create(final String name) {
        return TagKey.create(Registries.BLOCK, Constants.id(name));
    }

    static {
        REPLACEABLE_BASE_ROCKS = create("replaceable_base_rocks");
        BASE_STONE_OVERWORLD = create("base_stone_overworld");
    }
}
