package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Climate;

import java.util.Iterator;
import java.util.List;

/**
 * Lithome-specific parameter space.
 *
 * <p>The first implementation intentionally uses a compact three-dimensional
 * space instead of reusing vanilla's fixed six-dimensional climate point.</p>
 */
public final class LithomeClimate {

    private LithomeClimate() {
    }

    public static TargetPoint target(
            final float material,
            final float tectonics,
            final float continentalness
    ) {
        return new TargetPoint(
                Climate.quantizeCoord(material),
                Climate.quantizeCoord(tectonics),
                Climate.quantizeCoord(continentalness)
        );
    }

    public static ParameterPoint parameters(
            final Climate.Parameter material,
            final Climate.Parameter tectonics,
            final Climate.Parameter continentalness,
            final float offset
    ) {
        return new ParameterPoint(
                material,
                tectonics,
                continentalness,
                Climate.quantizeCoord(offset)
        );
    }

    public record TargetPoint(
            long material,
            long tectonics,
            long continentalness
    ) {
    }

    public record ParameterPoint(
            Climate.Parameter material,
            Climate.Parameter tectonics,
            Climate.Parameter continentalness,
            long offset
    ) {
        public static final Codec<ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Climate.Parameter.CODEC
                        .fieldOf("material")
                        .forGetter(ParameterPoint::material),
                Climate.Parameter.CODEC
                        .fieldOf("tectonics")
                        .forGetter(ParameterPoint::tectonics),
                Climate.Parameter.CODEC
                        .fieldOf("continentalness")
                        .forGetter(ParameterPoint::continentalness),
                Codec.floatRange(0.0F, 1.0F)
                        .fieldOf("offset")
                        .xmap(Climate::quantizeCoord, Climate::unquantizeCoord)
                        .forGetter(ParameterPoint::offset)
        ).apply(instance, ParameterPoint::new));

        public long fitness(final TargetPoint target) {
            return square(this.material.distance(target.material))
                    + square(this.tectonics.distance(target.tectonics))
                    + square(this.continentalness.distance(target.continentalness))
                    + square(this.offset);
        }

        private static long square(final long value) {
            return value * value;
        }
    }

    /**
     * A linear nearest-neighbour list.
     *
     * <p>The overworld preset currently contains only 17 points. At that size,
     * three interval-distance checks per point are negligible beside the four
     * noise samples used to construct the target point. Keeping this index
     * simple also avoids copying vanilla's private seven-dimensional R-tree.</p>
     */
    public static final class ParameterList<T> {
        private final List<Pair<ParameterPoint, T>> values;

        public static <T> Codec<ParameterList<T>> codec(final MapCodec<T> valueCodec) {
            final Codec<Pair<ParameterPoint, T>> entryCodec =
                    RecordCodecBuilder.<Pair<ParameterPoint, T>>create(instance -> instance.group(
                            ParameterPoint.CODEC
                                    .fieldOf("parameters")
                                    .forGetter((Pair<ParameterPoint, T> entry) -> entry.getFirst()),
                            valueCodec
                                    .forGetter((Pair<ParameterPoint, T> entry) -> entry.getSecond())
                    ).apply(instance, (parameters, value) -> Pair.of(parameters, value)));

            return ExtraCodecs.nonEmptyList(entryCodec.listOf())
                    .xmap(
                            values -> new ParameterList<T>(values),
                            parameterList -> parameterList.values()
                    );
        }

        public ParameterList(final List<Pair<ParameterPoint, T>> values) {
            if (values.isEmpty()) {
                throw new IllegalArgumentException("Lithome parameter list cannot be empty");
            }
            this.values = List.copyOf(values);
        }

        public List<Pair<ParameterPoint, T>> values() {
            return this.values;
        }

        public T findValue(final TargetPoint target) {
            final Iterator<Pair<ParameterPoint, T>> iterator = this.values.iterator();
            final Pair<ParameterPoint, T> first = iterator.next();

            long bestFitness = first.getFirst().fitness(target);
            T bestValue = first.getSecond();

            while (iterator.hasNext()) {
                final Pair<ParameterPoint, T> entry = iterator.next();
                final long fitness = entry.getFirst().fitness(target);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestValue = entry.getSecond();
                }
            }

            return bestValue;
        }
    }
}
