package io.github.simonxwei.lithome.world.level.lithome;

public final class FixedLithomeSource extends LithomeSource {

    private final Lithome lithome;

    public FixedLithomeSource(final Lithome lithome) {
        this.lithome = lithome;
    }

    // lithome resolver interface

    @Override
    public Lithome getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return this.lithome;
    }
}
