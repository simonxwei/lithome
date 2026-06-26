package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;

public final class FixedLithomeSource extends LithomeSource {

    public static final MapCodec<FixedLithomeSource> CODEC;

    private final Holder<Lithome> lithome;

    public FixedLithomeSource(final Holder<Lithome> lithome) {
        this.lithome = lithome;
    }

    // public

    @Override
    public MapCodec<FixedLithomeSource> codec() {
        return CODEC;
    }

    // lithome resolver interface

    @Override
    public Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return this.lithome;
    }

    static {
        CODEC = Lithome.CODEC.fieldOf("lithome").xmap(FixedLithomeSource::new, s -> s.lithome).stable();
    }
}
