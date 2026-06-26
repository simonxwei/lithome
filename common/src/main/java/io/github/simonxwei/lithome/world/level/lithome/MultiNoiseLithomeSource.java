package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;

public final class MultiNoiseLithomeSource extends LithomeSource {

    private static final MapCodec<Holder<Lithome>> ENTRY_CODEC;

    public static final MapCodec<Climate.ParameterList<Holder<Lithome>>> DIRECT_CODEC;
    public static final MapCodec<MultiNoiseLithomeSource> CODEC;

    private final Climate.ParameterList<Holder<Lithome>> parameters;

    public MultiNoiseLithomeSource(
            final Climate.ParameterList<Holder<Lithome>> parameters
    ) {
        this.parameters = parameters;
    }

    public Climate.ParameterList<Holder<Lithome>> parameters() {
        return this.parameters;
    }

    @Override
    protected MapCodec<MultiNoiseLithomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Lithome> getNoiseLithome(
            final int quartX,
            final int quartY,
            final int quartZ,
            final Climate.Sampler sampler
    ) {
        return this.parameters.findValue(
                sampler.sample(quartX, quartY, quartZ)
        );
    }

    static {
        ENTRY_CODEC = Lithome.CODEC.fieldOf("lithome");
        DIRECT_CODEC = Climate.ParameterList
                .codec(ENTRY_CODEC)
                .fieldOf("lithomes");
        CODEC = DIRECT_CODEC
                .xmap(MultiNoiseLithomeSource::new, MultiNoiseLithomeSource::parameters)
                .stable();
    }
}
