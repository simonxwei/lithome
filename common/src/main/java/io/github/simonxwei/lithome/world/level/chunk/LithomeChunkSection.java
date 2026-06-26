package io.github.simonxwei.lithome.world.level.chunk;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.PalettedContainerRO;

public interface LithomeChunkSection {

    boolean lithome$hasLithomes();

    Holder<Lithome> lithome$getNoiseLithome(int quartX, int quartY, int quartZ);

    void lithome$fillLithomesFromNoise(
            LithomeResolver resolver,
            Climate.Sampler sampler,
            int quartMinX,
            int quartMinY,
            int quartMinZ
    );

    PalettedContainerRO<Holder<Lithome>> lithome$getLithomes();

    void lithome$setLithomes(PalettedContainerRO<Holder<Lithome>> lithomes);
}
