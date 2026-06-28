package io.github.simonxwei.lithome.world.level.lithome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @see net.minecraft.world.level.biome.BiomeSource
 * @author simonxwei
 */
public abstract class LithomeSource implements LithomeResolver {

    public static final Codec<LithomeSource> CODEC;

    private final Supplier<Set<Holder<Lithome>>> possibleLithomes;

    protected LithomeSource() {
        this.possibleLithomes = Suppliers.memoize(() -> this.collectPossibleLithomes().distinct().collect(ImmutableSet.toImmutableSet()));
    }

    // public

    protected abstract MapCodec<? extends LithomeSource> codec();

    public Set<Holder<Lithome>> possibleLithomes() {
        return this.possibleLithomes.get();
    }

    public @Nullable Pair<BlockPos, Holder<Lithome>> findClosestLithome3d(final BlockPos origin, final int searchRadius, final int sampleResolutionHorizontal, final int sampleResolutionVertical, final Predicate<Holder<Lithome>> allowed, final LithomeClimateSampler sampler, final LevelReader level) {
        final Set<Holder<Lithome>> candidateLithomes = this.possibleLithomes().stream().filter(allowed).collect(ImmutableSet.toImmutableSet());

        if (candidateLithomes.isEmpty()) return null;

        final int sampleRadius = Math.floorDiv(searchRadius, sampleResolutionHorizontal);
        final int[] sampleYs = Mth.outFromOrigin(origin.getY(), level.getMinY() + 1, level.getMaxY() + 1, sampleResolutionVertical).toArray();

        for (final BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, sampleRadius, Direction.EAST, Direction.SOUTH)) {
            final int blockX = origin.getX() + sampleColumn.getX() * sampleResolutionHorizontal;
            final int blockZ = origin.getZ() + sampleColumn.getZ() * sampleResolutionHorizontal;
            final int noiseX = QuartPos.fromBlock(blockX);
            final int noiseZ = QuartPos.fromBlock(blockZ);

            for (final int blockY : sampleYs) {
                final int noiseY = QuartPos.fromBlock(blockY);
                final Holder<Lithome> lithome = this.getNoiseLithome(noiseX, noiseY, noiseZ, sampler);
                if (candidateLithomes.contains(lithome)) {
                    return Pair.of(new BlockPos(blockX, blockY, blockZ), lithome);
                }
            }
        }

        return null;
    }

    // core

    @Override
    public abstract Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ, final LithomeClimateSampler sampler);

    protected abstract Stream<Holder<Lithome>> collectPossibleLithomes();

    static {
        CODEC = LithomeBuiltInRegistries.LITHOME_SOURCE.byNameCodec().dispatchStable(LithomeSource::codec, Function.identity());
    }
}
