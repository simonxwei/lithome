package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;

import java.util.List;

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

    public static Climate.ParameterList<Holder<Lithome>> createTestParameters(
            final Holder<Lithome> deepslate,
            final Holder<Lithome> andesite,
            final Holder<Lithome> granite
    ) {
        return new Climate.ParameterList<>(List.of(
                Pair.of(parameterPoint(COLD_TEMPERATURE), deepslate),
                Pair.of(parameterPoint(TEMPERATE_TEMPERATURE), andesite),
                Pair.of(parameterPoint(WARM_TEMPERATURE), granite)
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
