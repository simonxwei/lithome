package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;

public abstract class LithomeSource implements LithomeResolver {

    protected LithomeSource() {}

    // lithome resolver interface

    @Override
    public abstract Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ);
}
