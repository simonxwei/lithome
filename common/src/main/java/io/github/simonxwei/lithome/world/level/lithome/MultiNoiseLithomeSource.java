package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @see net.minecraft.world.level.biome.MultiNoiseBiomeSource
 * @author simonxwei
 */
public final class MultiNoiseLithomeSource extends LithomeSource {

    private static final MapCodec<Holder<Lithome>> ENTRY_CODEC;
    private static final MapCodec<Holder<MultiNoiseLithomeSourceParameterList>> PRESET_CODEC;

    public static final MapCodec<LithomeClimate.ParameterList<Holder<Lithome>>> DIRECT_CODEC;
    public static final MapCodec<MultiNoiseLithomeSource> CODEC;

    private final Either<LithomeClimate.ParameterList<Holder<Lithome>>, Holder<MultiNoiseLithomeSourceParameterList>> parameters;

    private MultiNoiseLithomeSource(final Either<LithomeClimate.ParameterList<Holder<Lithome>>, Holder<MultiNoiseLithomeSourceParameterList>> parameters) {
        this.parameters = parameters;
    }

    // public

    @Override
    public MapCodec<MultiNoiseLithomeSource> codec() {
        return CODEC;
    }

    public static MultiNoiseLithomeSource createFromList(final LithomeClimate.ParameterList<Holder<Lithome>> parameters) {
        return new MultiNoiseLithomeSource(Either.left(parameters));
    }

    public static MultiNoiseLithomeSource createFromPreset(final Holder<MultiNoiseLithomeSourceParameterList> preset) {
        return new MultiNoiseLithomeSource(Either.right(preset));
    }

    public boolean stable(final ResourceKey<MultiNoiseLithomeSourceParameterList> expected) {
        final Optional<Holder<MultiNoiseLithomeSourceParameterList>> preset = this.parameters.right();
        return preset.isPresent() && preset.get().is(expected);
    }

    // core

    @Override
    public Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ, final LithomeClimateSampler sampler) {
        return this.parameters().findValue(sampler.sample(quartX, quartY, quartZ));
    }

    @Override
    protected Stream<Holder<Lithome>> collectPossibleLithomes() {
        return this.parameters().values().stream().map(Pair::getSecond);
    }

    // custom

    private LithomeClimate.ParameterList<Holder<Lithome>> parameters() {
        return this.parameters.map(direct -> direct, preset -> preset.value().parameters());
    }

    static {
        ENTRY_CODEC = Lithome.CODEC.fieldOf("lithome");
        PRESET_CODEC = MultiNoiseLithomeSourceParameterList.CODEC.fieldOf("preset").withLifecycle(Lifecycle.stable());
        DIRECT_CODEC = LithomeClimate.ParameterList.codec(ENTRY_CODEC).fieldOf("lithomes");
        CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC).xmap(MultiNoiseLithomeSource::new, s -> s.parameters);
    }
}
