package io.github.simonxwei.lithome.world.level.lithome;

public abstract class LithomeSource implements LithomeResolver {

    protected LithomeSource() {}

    // lithome resolver interface

    @Override
    public abstract Lithome getNoiseLithome(final int quartX, final int quartY, final int quartZ);
}
