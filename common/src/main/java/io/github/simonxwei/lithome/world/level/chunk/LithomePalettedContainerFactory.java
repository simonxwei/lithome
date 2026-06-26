package io.github.simonxwei.lithome.world.level.chunk;

import com.mojang.serialization.Codec;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;

public interface LithomePalettedContainerFactory {

    void lithome$initializeLithomes(RegistryAccess registries);

    boolean lithome$hasLithomeSupport();

    PalettedContainer<Holder<Lithome>> lithome$createForLithomes();

    Codec<PalettedContainerRO<Holder<Lithome>>> lithome$lithomeContainerCodec();
}
