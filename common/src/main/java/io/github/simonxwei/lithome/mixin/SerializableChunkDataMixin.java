package io.github.simonxwei.lithome.mixin;

import com.mojang.serialization.Codec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkSection;
import io.github.simonxwei.lithome.world.level.chunk.LithomePalettedContainerFactory;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * @author simonxwei
 */
@Mixin(SerializableChunkData.class)
public abstract class SerializableChunkDataMixin {

    @Unique
    private static final String LITHOME_TAG = "lithomes";

    @Inject(method = "parse", at = @At("RETURN"))
    private static void lithome$readLithomeContainers(
            final LevelHeightAccessor levelHeight,
            final PalettedContainerFactory containerFactory,
            final CompoundTag chunkData,
            final CallbackInfoReturnable<SerializableChunkData> cir
    ) {
        final SerializableChunkData data = cir.getReturnValue();
        if (data == null) return;

        final LithomePalettedContainerFactory lithomeFactory = (LithomePalettedContainerFactory) (Object) containerFactory;
        final Codec<PalettedContainerRO<Holder<Lithome>>> codec = lithomeFactory.lithome$lithomeContainerCodec();
        final ListTag sectionTags = chunkData.getListOrEmpty(SerializableChunkData.SECTIONS_TAG);

        for (final SerializableChunkData.SectionData sectionData : data.sectionData()) {
            final LevelChunkSection section = sectionData.chunkSection();
            if (section == null) continue;

            final PalettedContainerRO<Holder<Lithome>> lithomes = lithome$findSectionTag(sectionTags, sectionData.y())
                    .flatMap(sectionTag -> sectionTag.getCompound(LITHOME_TAG))
                    .map(container -> codec
                            .parse(NbtOps.INSTANCE, container)
                            .promotePartial(message -> Constants.LOGGER.error("Failed to decode Lithome palette in chunk {} section {}: {}", data.chunkPos(), sectionData.y(), message))
                            .getOrThrow(SerializableChunkData.ChunkReadException::new))
                    .orElseGet(lithomeFactory::lithome$createForLithomes);

            ((LithomeChunkSection) section).lithome$setLithomes(lithomes);
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void lithome$writeLithomeContainers(final CallbackInfoReturnable<CompoundTag> cir) {
        final SerializableChunkData data = (SerializableChunkData) (Object) this;
        final CompoundTag chunkData = cir.getReturnValue();
        final ListTag sectionTags = chunkData.getListOrEmpty(SerializableChunkData.SECTIONS_TAG);
        final Codec<PalettedContainerRO<Holder<Lithome>>> codec = ((LithomePalettedContainerFactory) (Object) data.containerFactory()).lithome$lithomeContainerCodec();

        for (final SerializableChunkData.SectionData sectionData : data.sectionData()) {
            final LevelChunkSection section = sectionData.chunkSection();
            if (section == null) continue;

            final CompoundTag sectionTag = lithome$findSectionTag(
                    sectionTags,
                    sectionData.y()).orElseThrow(() -> new IllegalStateException("Missing serialized section " + sectionData.y() + " while writing Lithome data for chunk " + data.chunkPos())
            );
            sectionTag.store(LITHOME_TAG, codec, ((LithomeChunkSection) section).lithome$getLithomes());
        }
    }

    @Unique
    private static Optional<CompoundTag> lithome$findSectionTag(final ListTag sectionTags, final int sectionY) {
        for (int index = 0; index < sectionTags.size(); ++index) {
            final Optional<CompoundTag> sectionTag = sectionTags.getCompound(index);
            if (sectionTag.isPresent() && sectionTag.get().getByteOr("Y", (byte) 0) == (byte) sectionY) {
                return sectionTag;
            }
        }
        return Optional.empty();
    }
}
