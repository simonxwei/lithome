package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

/**
 * 主要参考的 Minecraft 原版源码类：
 * - net.minecraft.world.level.biome.BiomeManager
 * - net.minecraft.world.level.levelgen.SurfaceSystem
 *
 * 本类针对区块体积扫描进行批量缓存。
 *
 * @author simonxwei
 */
public final class LithomeVolumeSampler {
    private static final int CELL_WIDTH = 4;
    private static final int CELL_MASK = CELL_WIDTH - 1;
    private static final int CORNER_COUNT = 8;

    private final LithomeManager.NoiseLithomeSource noiseLithomeSource;
    private final long lithomeZoomSeed;

    private final int minimumParentX;
    private final int minimumParentY;
    private final int minimumParentZ;
    private final int parentCountX;
    private final int parentCountY;
    private final int parentCountZ;

    private final @Nullable Cell[] cells;

    private long createdCells;

    public LithomeVolumeSampler(
        final LithomeManager.NoiseLithomeSource noiseLithomeSource,
        final long lithomeZoomSeed,
        final int minimumBlockX,
        final int minimumBlockY,
        final int minimumBlockZ,
        final int maximumBlockYExclusive
    ) {
        if (maximumBlockYExclusive <= minimumBlockY) {
            throw new IllegalArgumentException("maximumBlockYExclusive must exceed minimumBlockY");
        }

        this.noiseLithomeSource = noiseLithomeSource;
        this.lithomeZoomSeed = lithomeZoomSeed;

        this.minimumParentX = parentCoordinate(minimumBlockX);
        this.minimumParentY = parentCoordinate(minimumBlockY);
        this.minimumParentZ = parentCoordinate(minimumBlockZ);

        final int maximumParentX = parentCoordinate(minimumBlockX + 15);
        final int maximumParentY = parentCoordinate(maximumBlockYExclusive - 1);
        final int maximumParentZ = parentCoordinate(minimumBlockZ + 15);

        this.parentCountX = maximumParentX - this.minimumParentX + 1;
        this.parentCountY = maximumParentY - this.minimumParentY + 1;
        this.parentCountZ = maximumParentZ - this.minimumParentZ + 1;
        this.cells = new Cell[this.parentCountX * this.parentCountY * this.parentCountZ];
    }

    // public

    public Holder<Lithome> getLithome(
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        final int absoluteX = blockX - 2;
        final int absoluteY = blockY - 2;
        final int absoluteZ = blockZ - 2;

        final int parentX = absoluteX >> 2;
        final int parentY = absoluteY >> 2;
        final int parentZ = absoluteZ >> 2;
        final int cellIndex = this.cellIndex(parentX, parentY, parentZ);

        Cell cell = this.cells[cellIndex];
        if (cell == null) {
            cell = new Cell(parentX, parentY, parentZ);
            this.cells[cellIndex] = cell;
            ++this.createdCells;
        }

        return cell.getLithome(
            absoluteX & CELL_MASK,
            absoluteY & CELL_MASK,
            absoluteZ & CELL_MASK
        );
    }

    public long createdCells() {
        return this.createdCells;
    }

    public long computedFiddleOffsets() {
        return this.createdCells * CORNER_COUNT;
    }

    // core

    private static int parentCoordinate(final int blockCoordinate) {
        return (blockCoordinate - 2) >> 2;
    }

    private int cellIndex(
        final int parentX,
        final int parentY,
        final int parentZ
    ) {
        final int localParentX = parentX - this.minimumParentX;
        final int localParentY = parentY - this.minimumParentY;
        final int localParentZ = parentZ - this.minimumParentZ;

        if (
            localParentX < 0 || localParentX >= this.parentCountX
                || localParentY < 0 || localParentY >= this.parentCountY
                || localParentZ < 0 || localParentZ >= this.parentCountZ
        ) {
            throw new IllegalArgumentException(
                "Block query lies outside this Lithome volume sampler: parent="
                    + parentX + "," + parentY + "," + parentZ
            );
        }

        return (localParentX * this.parentCountZ + localParentZ)
            * this.parentCountY + localParentY;
    }

    private final class Cell {
        private final int parentX;
        private final int parentY;
        private final int parentZ;

        private final double[] fiddleX = new double[CORNER_COUNT];
        private final double[] fiddleY = new double[CORNER_COUNT];
        private final double[] fiddleZ = new double[CORNER_COUNT];

        private final @Nullable Holder<Lithome>[] lithomes;

        @SuppressWarnings("unchecked")
        private Cell(
            final int parentX,
            final int parentY,
            final int parentZ
        ) {
            this.parentX = parentX;
            this.parentY = parentY;
            this.parentZ = parentZ;
            this.lithomes = (Holder<Lithome>[]) new Holder<?>[CORNER_COUNT];

            for (int corner = 0; corner < CORNER_COUNT; ++corner) {
                final int cornerX = parentX + ((corner & 4) == 0 ? 0 : 1);
                final int cornerY = parentY + ((corner & 2) == 0 ? 0 : 1);
                final int cornerZ = parentZ + ((corner & 1) == 0 ? 0 : 1);

                long random = LinearCongruentialGenerator.next(
                    LithomeVolumeSampler.this.lithomeZoomSeed,
                    cornerX
                );
                random = LinearCongruentialGenerator.next(random, cornerY);
                random = LinearCongruentialGenerator.next(random, cornerZ);
                random = LinearCongruentialGenerator.next(random, cornerX);
                random = LinearCongruentialGenerator.next(random, cornerY);
                random = LinearCongruentialGenerator.next(random, cornerZ);

                this.fiddleX[corner] = getFiddle(random);
                random = LinearCongruentialGenerator.next(
                    random,
                    LithomeVolumeSampler.this.lithomeZoomSeed
                );
                this.fiddleY[corner] = getFiddle(random);
                random = LinearCongruentialGenerator.next(
                    random,
                    LithomeVolumeSampler.this.lithomeZoomSeed
                );
                this.fiddleZ[corner] = getFiddle(random);
            }
        }

        private Holder<Lithome> getLithome(
            final int localX,
            final int localY,
            final int localZ
        ) {
            final double fractionalX = (double) localX / CELL_WIDTH;
            final double fractionalY = (double) localY / CELL_WIDTH;
            final double fractionalZ = (double) localZ / CELL_WIDTH;

            int nearestCorner = 0;
            double nearestDistance = Double.POSITIVE_INFINITY;

            for (int corner = 0; corner < CORNER_COUNT; ++corner) {
                final double distanceX = (corner & 4) == 0
                    ? fractionalX
                    : fractionalX - 1.0D;
                final double distanceY = (corner & 2) == 0
                    ? fractionalY
                    : fractionalY - 1.0D;
                final double distanceZ = (corner & 1) == 0
                    ? fractionalZ
                    : fractionalZ - 1.0D;
                final double fiddledDistance =
                    Mth.square(distanceX + this.fiddleX[corner])
                        + Mth.square(distanceY + this.fiddleY[corner])
                        + Mth.square(distanceZ + this.fiddleZ[corner]);

                if (fiddledDistance < nearestDistance) {
                    nearestCorner = corner;
                    nearestDistance = fiddledDistance;
                }
            }

            Holder<Lithome> lithome = this.lithomes[nearestCorner];
            if (lithome == null) {
                final int quartX = this.parentX + ((nearestCorner & 4) == 0 ? 0 : 1);
                final int quartY = this.parentY + ((nearestCorner & 2) == 0 ? 0 : 1);
                final int quartZ = this.parentZ + ((nearestCorner & 1) == 0 ? 0 : 1);
                lithome = LithomeVolumeSampler.this.noiseLithomeSource.lithome$getNoiseLithome(
                    quartX,
                    quartY,
                    quartZ
                );
                this.lithomes[nearestCorner] = lithome;
            }

            return lithome;
        }
    }

    private static double getFiddle(final long random) {
        final double uniform = (double) Math.floorMod(random >> 24, 1024) / 1024.0D;
        return (uniform - 0.5D) * 0.9D;
    }
}
