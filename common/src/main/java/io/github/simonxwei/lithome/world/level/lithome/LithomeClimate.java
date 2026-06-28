package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;

import java.util.Iterator;
import java.util.List;

/**
 * @see Climate
 * @author simonxwei
 */
public final class LithomeClimate {

    private LithomeClimate() {}

    // public

    public static TargetPoint target(final float material, final float tectonics, final float continentalness) {
        return new TargetPoint(Climate.quantizeCoord(material), Climate.quantizeCoord(tectonics), Climate.quantizeCoord(continentalness));
    }

    public static ParameterPoint parameters(final Climate.Parameter material, final Climate.Parameter tectonics, final Climate.Parameter continentalness, final float offset) {
        return new ParameterPoint(material, tectonics, continentalness, Climate.quantizeCoord(offset));
    }

    public record TargetPoint(long material, long tectonics, long continentalness) {}

    public record ParameterPoint(Climate.Parameter material, Climate.Parameter tectonics, Climate.Parameter continentalness, long offset) {

        public static final Codec<ParameterPoint> CODEC;

        public long fitness(final TargetPoint target) {
            return Mth.square(this.material.distance(target.material))
                    + Mth.square(this.tectonics.distance(target.tectonics))
                    + Mth.square(this.continentalness.distance(target.continentalness))
                    + Mth.square(this.offset);
        }

        static {
            CODEC = RecordCodecBuilder.create(i -> i.group(
                    Climate.Parameter.CODEC.fieldOf("material").forGetter(ParameterPoint::material),
                    Climate.Parameter.CODEC.fieldOf("tectonics").forGetter(ParameterPoint::tectonics),
                    Climate.Parameter.CODEC.fieldOf("continentalness").forGetter(ParameterPoint::continentalness),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter(ParameterPoint::offset)
            ).apply(i, ParameterPoint::new));
        }
    }

    public record ParameterList<T>(List<Pair<ParameterPoint, T>> values) {

        public static <T> Codec<ParameterList<T>> codec(final MapCodec<T> valueCodec) {
            final Codec<Pair<ParameterPoint, T>> entryCodec = RecordCodecBuilder.create(i -> i.group(
                    ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
                    valueCodec.forGetter(Pair::getSecond)
            ).apply(i, Pair::of));

            return ExtraCodecs.nonEmptyList(entryCodec.listOf()).xmap(ParameterList::new, ParameterList::values);
        }

        public T findValue(final TargetPoint target) {
            final Iterator<Pair<ParameterPoint, T>> iterator = this.values.iterator();
            final Pair<ParameterPoint, T> first = iterator.next();
            long bestFitness = first.getFirst().fitness(target);
            T bestValue = first.getSecond();

            while (iterator.hasNext()) {
                final Pair<ParameterPoint, T> parameter = iterator.next();
                final long fitness = parameter.getFirst().fitness(target);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestValue = parameter.getSecond();
                }
            }

            return bestValue;
        }
    }
}
