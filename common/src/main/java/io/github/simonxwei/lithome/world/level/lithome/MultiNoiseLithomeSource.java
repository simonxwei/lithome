package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;

import java.util.Optional;
import java.util.stream.Stream;

public final class MultiNoiseLithomeSource extends LithomeSource {

    private static final MapCodec<Holder<Lithome>> ENTRY_CODEC;

    public static final MapCodec<Climate.ParameterList<Holder<Lithome>>> DIRECT_CODEC;
    private static final MapCodec<Holder<MultiNoiseLithomeSourceParameterList>> PRESET_CODEC;
    public static final MapCodec<MultiNoiseLithomeSource> CODEC;

    private final Either<
            Climate.ParameterList<Holder<Lithome>>,
            Holder<MultiNoiseLithomeSourceParameterList>
            > parameters;

    private MultiNoiseLithomeSource(
            final Either<
                    Climate.ParameterList<Holder<Lithome>>,
                    Holder<MultiNoiseLithomeSourceParameterList>
                    > parameters
    ) {
        this.parameters = parameters;
    }

    public static MultiNoiseLithomeSource createFromList(
            final Climate.ParameterList<Holder<Lithome>> parameters
    ) {
        return new MultiNoiseLithomeSource(Either.left(parameters));
    }

    public static MultiNoiseLithomeSource createFromPreset(
            final Holder<MultiNoiseLithomeSourceParameterList> preset
    ) {
        return new MultiNoiseLithomeSource(Either.right(preset));
    }

    private Climate.ParameterList<Holder<Lithome>> parameters() {
        return this.parameters.map(
                direct -> direct,
                preset -> preset.value().parameters()
        );
    }

    public boolean stable(
            final ResourceKey<MultiNoiseLithomeSourceParameterList> expected
    ) {
        final Optional<Holder<MultiNoiseLithomeSourceParameterList>> preset =
                this.parameters.right();
        return preset.isPresent() && preset.get().is(expected);
    }

    @Override
    protected MapCodec<MultiNoiseLithomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Lithome>> collectPossibleLithomes() {
        return this.parameters().values().stream().map(Pair::getSecond);
    }

    @Override
    public Holder<Lithome> getNoiseLithome(
            final int quartX,
            final int quartY,
            final int quartZ,
            final Climate.Sampler sampler
    ) {
        return this.parameters().findValue(
                sampler.sample(quartX, quartY, quartZ)
        );
    }

    static {
        ENTRY_CODEC = Lithome.CODEC.fieldOf("lithome");
        DIRECT_CODEC = Climate.ParameterList
                .codec(ENTRY_CODEC)
                .fieldOf("lithomes");
        PRESET_CODEC = MultiNoiseLithomeSourceParameterList.CODEC
                .fieldOf("preset")
                .withLifecycle(Lifecycle.stable());
        CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC)
                .xmap(MultiNoiseLithomeSource::new, source -> source.parameters);
    }
}
