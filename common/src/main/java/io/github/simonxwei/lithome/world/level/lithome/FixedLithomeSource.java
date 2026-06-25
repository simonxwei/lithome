package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;

public final class FixedLithomeSource extends LithomeSource {

    private final Holder<Lithome> lithome;

    public FixedLithomeSource(final Holder<Lithome> lithome) {
        this.lithome = lithome;
    }

    // lithome resolver interface

    @Override
    public Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return this.lithome;
    }
}
