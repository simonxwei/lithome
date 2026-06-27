package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MultiNoiseLithomeSourceParameterList {
    public static final Codec<MultiNoiseLithomeSourceParameterList> DIRECT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Preset.CODEC
                            .fieldOf("preset")
                            .forGetter(parameterList -> parameterList.preset),
                    RegistryOps.retrieveGetter(LithomeRegistries.LITHOME)
            ).apply(instance, MultiNoiseLithomeSourceParameterList::new));

    public static final Codec<Holder<MultiNoiseLithomeSourceParameterList>> CODEC =
            RegistryFileCodec.create(
                    LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST,
                    DIRECT_CODEC
            );

    private final Preset preset;
    private final LithomeClimate.ParameterList<Holder<Lithome>> parameters;

    public MultiNoiseLithomeSourceParameterList(
            final Preset preset,
            final HolderGetter<Lithome> lithomes
    ) {
        this.preset = preset;
        final Preset.SourceProvider provider = preset.provider();
        Objects.requireNonNull(lithomes);
        this.parameters = provider.apply(lithomes::getOrThrow);
    }

    public LithomeClimate.ParameterList<Holder<Lithome>> parameters() {
        return this.parameters;
    }

    public record Preset(
            Identifier id,
            SourceProvider provider
    ) {
        public static final Preset OVERWORLD = new Preset(
                Constants.id("overworld"),
                new SourceProvider() {
                    @Override
                    public <T> LithomeClimate.ParameterList<T> apply(
                            final Function<ResourceKey<Lithome>, T> lookup
                    ) {
                        return OverworldLithomeBuilder.createParameters(lookup);
                    }
                }
        );

        private static final Map<Identifier, Preset> BY_NAME = Stream.of(OVERWORLD)
                .collect(Collectors.toMap(Preset::id, preset -> preset));

        public static final Codec<Preset> CODEC = Identifier.CODEC.flatXmap(
                identifier -> Optional.ofNullable(BY_NAME.get(identifier))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown Lithome parameter preset: " + identifier
                        )),
                preset -> DataResult.success(preset.id())
        );

        @FunctionalInterface
        private interface SourceProvider {
            <T> LithomeClimate.ParameterList<T> apply(
                    Function<ResourceKey<Lithome>, T> lookup
            );
        }
    }
}
