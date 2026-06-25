package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;

public interface LithomeResolver {

    Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ);
}
