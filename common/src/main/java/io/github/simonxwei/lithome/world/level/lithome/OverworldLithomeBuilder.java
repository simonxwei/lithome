package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.function.Function;

public final class OverworldLithomeBuilder {

    private static final Climate.Parameter FULL_RANGE =
            Climate.Parameter.span(-2.0F, 2.0F);

    private static final Climate.Parameter COLD_TEMPERATURE =
            Climate.Parameter.span(-2.0F, -0.4F);
    private static final Climate.Parameter TEMPERATE_TEMPERATURE =
            Climate.Parameter.span(-0.4F, 0.4F);
    private static final Climate.Parameter WARM_TEMPERATURE =
            Climate.Parameter.span(0.4F, 2.0F);

    private OverworldLithomeBuilder() {
    }

    public static <T> Climate.ParameterList<T> createTestParameters(
            final Function<ResourceKey<Lithome>, T> lookup
    ) {
        return new Climate.ParameterList<>(List.of(
                Pair.of(
                        parameterPoint(COLD_TEMPERATURE),
                        lookup.apply(Lithomes.DEEPSLATE)
                ),
                Pair.of(
                        parameterPoint(TEMPERATE_TEMPERATURE),
                        lookup.apply(Lithomes.ANDESITE)
                ),
                Pair.of(
                        parameterPoint(WARM_TEMPERATURE),
                        lookup.apply(Lithomes.GRANITE)
                )
        ));
    }

    private static Climate.ParameterPoint parameterPoint(
            final Climate.Parameter temperature
    ) {
        return Climate.parameters(
                temperature,
                FULL_RANGE,
                FULL_RANGE,
                FULL_RANGE,
                FULL_RANGE,
                FULL_RANGE,
                0.0F
        );
    }
}
