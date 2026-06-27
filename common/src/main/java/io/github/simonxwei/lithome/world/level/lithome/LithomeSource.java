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

public abstract class LithomeSource implements LithomeResolver {
    public static final Codec<LithomeSource> CODEC;

    private final Supplier<Set<Holder<Lithome>>> possibleLithomes = Suppliers.memoize(
            () -> this.collectPossibleLithomes()
                    .distinct()
                    .collect(ImmutableSet.toImmutableSet())
    );

    protected LithomeSource() {
    }

    protected abstract MapCodec<? extends LithomeSource> codec();

    protected abstract Stream<Holder<Lithome>> collectPossibleLithomes();

    public Set<Holder<Lithome>> possibleLithomes() {
        return this.possibleLithomes.get();
    }

    public @Nullable Pair<BlockPos, Holder<Lithome>> findClosestLithome3d(
            final BlockPos origin,
            final int searchRadius,
            final int horizontalResolution,
            final int verticalResolution,
            final Predicate<Holder<Lithome>> allowed,
            final LithomeSampler sampler,
            final LevelReader level
    ) {
        final Set<Holder<Lithome>> candidates = this.possibleLithomes()
                .stream()
                .filter(allowed)
                .collect(ImmutableSet.toImmutableSet());
        if (candidates.isEmpty()) {
            return null;
        }

        final int sampleRadius = Math.floorDiv(searchRadius, horizontalResolution);
        final int[] sampleYs = Mth.outFromOrigin(
                origin.getY(),
                level.getMinY() + 1,
                level.getMaxY() + 1,
                verticalResolution
        ).toArray();

        for (final BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(
                BlockPos.ZERO,
                sampleRadius,
                Direction.EAST,
                Direction.SOUTH
        )) {
            final int blockX = origin.getX() + sampleColumn.getX() * horizontalResolution;
            final int blockZ = origin.getZ() + sampleColumn.getZ() * horizontalResolution;
            final int quartX = QuartPos.fromBlock(blockX);
            final int quartZ = QuartPos.fromBlock(blockZ);

            for (final int blockY : sampleYs) {
                final int quartY = QuartPos.fromBlock(blockY);
                final Holder<Lithome> lithome = this.getNoiseLithome(
                        quartX,
                        quartY,
                        quartZ,
                        sampler
                );
                if (candidates.contains(lithome)) {
                    return Pair.of(new BlockPos(blockX, blockY, blockZ), lithome);
                }
            }
        }

        return null;
    }

    @Override
    public abstract Holder<Lithome> getNoiseLithome(
            int quartX,
            int quartY,
            int quartZ,
            LithomeSampler sampler
    );

    static {
        CODEC = LithomeBuiltInRegistries.LITHOME_SOURCE
                .byNameCodec()
                .dispatchStable(LithomeSource::codec, Function.identity());
    }
}
