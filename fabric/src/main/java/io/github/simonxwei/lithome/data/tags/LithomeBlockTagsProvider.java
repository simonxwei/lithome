package io.github.simonxwei.lithome.data.tags;

import io.github.simonxwei.lithome.tags.LithomeBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public final class LithomeBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {

    public LithomeBlockTagsProvider(final FabricPackOutput output, final CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(final HolderLookup.Provider registries) {
        this.tag(LithomeBlockTags.BASE_STONE_OVERWORLD)
                .addOptionalTag(BlockTags.BASE_STONE_OVERWORLD)
                .add(BlockItemIds.CALCITE.block());
    }
}
