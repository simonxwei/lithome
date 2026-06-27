package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;

import java.util.stream.Stream;

public final class FixedLithomeSource extends LithomeSource {
    public static final MapCodec<FixedLithomeSource> CODEC;

    private final Holder<Lithome> lithome;

    public FixedLithomeSource(final Holder<Lithome> lithome) {
        this.lithome = lithome;
    }

    @Override
    protected MapCodec<? extends LithomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Lithome>> collectPossibleLithomes() {
        return Stream.of(this.lithome);
    }

    @Override
    public Holder<Lithome> getNoiseLithome(
            final int quartX,
            final int quartY,
            final int quartZ,
            final LithomeSampler sampler
    ) {
        return this.lithome;
    }

    static {
        CODEC = Lithome.CODEC
                .fieldOf("lithome")
                .xmap(FixedLithomeSource::new, source -> source.lithome)
                .stable();
    }
}
