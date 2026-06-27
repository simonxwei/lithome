package io.github.simonxwei.lithome.data.tags;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.tags.LithomeBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public final class LithomeBlockTagsProvider extends BlockTagsProvider {

    public LithomeBlockTagsProvider(
            final PackOutput output,
            final CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, lookupProvider, Constants.MOD_ID);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        this.tag(LithomeBlockTags.BASE_STONE_OVERWORLD)
                .addOptionalTag(BlockTags.BASE_STONE_OVERWORLD)
                .add(BlockItemIds.CALCITE.block());
    }
}
