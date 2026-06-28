package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;

import java.util.stream.Stream;

/**
 * @see net.minecraft.world.level.biome.FixedBiomeSource
 * @author simonxwei
 */
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

    // core

    @Override
    public Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ, final LithomeClimateSampler sampler) {
        return this.lithome;
    }

    @Override
    protected Stream<Holder<Lithome>> collectPossibleLithomes() {
        return Stream.of(this.lithome);
    }

    static {
        CODEC = Lithome.CODEC.fieldOf("lithome").xmap(FixedLithomeSource::new, s -> s.lithome).stable();
    }
}
