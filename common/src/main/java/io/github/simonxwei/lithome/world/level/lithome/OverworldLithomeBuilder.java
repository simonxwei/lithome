package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.function.Function;

public final class OverworldLithomeBuilder {
    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-2.0F, 2.0F);

    private static final Climate.Parameter MATERIAL_DARK =
            Climate.Parameter.span(-2.0F, -0.45F);
    private static final Climate.Parameter MATERIAL_DARK_NEUTRAL =
            Climate.Parameter.span(-0.45F, -0.15F);
    private static final Climate.Parameter MATERIAL_LIGHT_NEUTRAL =
            Climate.Parameter.span(-0.15F, 0.20F);
    private static final Climate.Parameter MATERIAL_LIGHT_WARM =
            Climate.Parameter.span(0.20F, 2.0F);

    private static final Climate.Parameter TECTONICS_STRONG_BASIN =
            Climate.Parameter.span(-2.0F, -0.50F);
    private static final Climate.Parameter TECTONICS_WEAK_BASIN =
            Climate.Parameter.span(-0.50F, -0.15F);
    private static final Climate.Parameter TECTONICS_STABLE =
            Climate.Parameter.span(-0.15F, 0.35F);
    private static final Climate.Parameter TECTONICS_OROGENIC =
            Climate.Parameter.span(0.35F, 2.0F);

    private static final Climate.Parameter CONTINENTALNESS_OCEAN_TO_NEAR_INLAND =
            Climate.Parameter.span(-2.0F, 0.03F);
    private static final Climate.Parameter CONTINENTALNESS_INLAND =
            Climate.Parameter.span(0.03F, 2.0F);

    private OverworldLithomeBuilder() {
    }

    public static <T> LithomeClimate.ParameterList<T> createParameters(
            final Function<ResourceKey<Lithome>, T> lookup
    ) {
        return new LithomeClimate.ParameterList<>(List.of(
                // Strong basin / extension tendency.
                entry(MATERIAL_DARK, TECTONICS_STRONG_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.ANDESITE)),
                entry(MATERIAL_DARK_NEUTRAL, TECTONICS_STRONG_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.STONE)),
                entry(MATERIAL_LIGHT_NEUTRAL, TECTONICS_STRONG_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.CALCITE)),
                entry(MATERIAL_LIGHT_WARM, TECTONICS_STRONG_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.GRANITE)),

                // Weak basin tendency. Continentalness only modifies one cell.
                entry(MATERIAL_DARK, TECTONICS_WEAK_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.ANDESITE)),
                entry(MATERIAL_DARK_NEUTRAL, TECTONICS_WEAK_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.STONE)),
                entry(MATERIAL_LIGHT_NEUTRAL, TECTONICS_WEAK_BASIN,
                        CONTINENTALNESS_OCEAN_TO_NEAR_INLAND,
                        lookup.apply(Lithomes.CALCITE)),
                entry(MATERIAL_LIGHT_NEUTRAL, TECTONICS_WEAK_BASIN,
                        CONTINENTALNESS_INLAND,
                        lookup.apply(Lithomes.STONE)),
                entry(MATERIAL_LIGHT_WARM, TECTONICS_WEAK_BASIN, FULL_RANGE,
                        lookup.apply(Lithomes.GRANITE)),

                // Stable background.
                entry(MATERIAL_DARK, TECTONICS_STABLE, FULL_RANGE,
                        lookup.apply(Lithomes.ANDESITE)),
                entry(MATERIAL_DARK_NEUTRAL, TECTONICS_STABLE, FULL_RANGE,
                        lookup.apply(Lithomes.STONE)),
                entry(MATERIAL_LIGHT_NEUTRAL, TECTONICS_STABLE, FULL_RANGE,
                        lookup.apply(Lithomes.STONE)),
                entry(MATERIAL_LIGHT_WARM, TECTONICS_STABLE, FULL_RANGE,
                        lookup.apply(Lithomes.GRANITE)),

                // Orogenic / compression tendency.
                entry(MATERIAL_DARK, TECTONICS_OROGENIC, FULL_RANGE,
                        lookup.apply(Lithomes.DEEPSLATE)),
                entry(MATERIAL_DARK_NEUTRAL, TECTONICS_OROGENIC, FULL_RANGE,
                        lookup.apply(Lithomes.ANDESITE)),
                entry(MATERIAL_LIGHT_NEUTRAL, TECTONICS_OROGENIC, FULL_RANGE,
                        lookup.apply(Lithomes.GRANITE)),
                entry(MATERIAL_LIGHT_WARM, TECTONICS_OROGENIC, FULL_RANGE,
                        lookup.apply(Lithomes.GRANITE))
        ));
    }

    private static <T> Pair<LithomeClimate.ParameterPoint, T> entry(
            final Climate.Parameter material,
            final Climate.Parameter tectonics,
            final Climate.Parameter continentalness,
            final T lithome
    ) {
        return Pair.of(
                LithomeClimate.parameters(
                        material,
                        tectonics,
                        continentalness,
                        0.0F
                ),
                lithome
        );
    }
}
