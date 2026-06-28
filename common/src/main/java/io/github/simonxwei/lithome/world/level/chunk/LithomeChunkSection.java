package io.github.simonxwei.lithome.world.level.chunk;

import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.PalettedContainerRO;

/**
 * @author simonxwei
 */
public interface LithomeChunkSection {

    boolean lithome$hasLithomes();

    Holder<Lithome> lithome$getNoiseLithome(final int quartX, final int quartY, final int quartZ);

    void lithome$fillLithomesFromNoise(final LithomeResolver resolver, final LithomeClimateSampler sampler, final int quartMinX, final int quartMinY, final int quartMinZ);

    PalettedContainerRO<Holder<Lithome>> lithome$getLithomes();

    void lithome$setLithomes(final PalettedContainerRO<Holder<Lithome>> lithomes);
}
