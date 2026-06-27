package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeManager;

public final class LithomeManager {

    private static final long ZOOM_SEED_SALT = 0x4C4954484F4D45L;

    private final NoiseLithomeSource noiseLithomeSource;
    private final long lithomeZoomSeed;

    public LithomeManager(final NoiseLithomeSource noiseLithomeSource, final long worldSeed) {
        this(noiseLithomeSource, BiomeManager.obfuscateSeed(worldSeed ^ ZOOM_SEED_SALT), true);
    }

    private LithomeManager(
            final NoiseLithomeSource noiseLithomeSource,
            final long lithomeZoomSeed,
            final boolean ignored
    ) {
        this.noiseLithomeSource = noiseLithomeSource;
        this.lithomeZoomSeed = lithomeZoomSeed;
    }

    public LithomeManager withDifferentSource(final NoiseLithomeSource source) {
        return new LithomeManager(source, this.lithomeZoomSeed, true);
    }

    public Holder<Lithome> getLithome(final BlockPos pos) {
        return this.getLithomeSelection(pos).lithome();
    }

    public Selection getLithomeSelection(final BlockPos pos) {
        final int absoluteX = pos.getX() - 2;
        final int absoluteY = pos.getY() - 2;
        final int absoluteZ = pos.getZ() - 2;
        final int parentX = absoluteX >> 2;
        final int parentY = absoluteY >> 2;
        final int parentZ = absoluteZ >> 2;
        final double fractionalX = (double) (absoluteX & 3) / 4.0D;
        final double fractionalY = (double) (absoluteY & 3) / 4.0D;
        final double fractionalZ = (double) (absoluteZ & 3) / 4.0D;
        int nearestCorner = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (int corner = 0; corner < 8; ++corner) {
            final boolean lowerX = (corner & 4) == 0;
            final boolean lowerY = (corner & 2) == 0;
            final boolean lowerZ = (corner & 1) == 0;
            final int cornerX = lowerX ? parentX : parentX + 1;
            final int cornerY = lowerY ? parentY : parentY + 1;
            final int cornerZ = lowerZ ? parentZ : parentZ + 1;
            final double distanceX = lowerX ? fractionalX : fractionalX - 1.0D;
            final double distanceY = lowerY ? fractionalY : fractionalY - 1.0D;
            final double distanceZ = lowerZ ? fractionalZ : fractionalZ - 1.0D;
            final double fiddledDistance = getFiddledDistance(
                    this.lithomeZoomSeed,
                    cornerX,
                    cornerY,
                    cornerZ,
                    distanceX,
                    distanceY,
                    distanceZ
            );
            if (fiddledDistance < nearestDistance) {
                nearestCorner = corner;
                nearestDistance = fiddledDistance;
            }
        }

        final int lithomeX = (nearestCorner & 4) == 0 ? parentX : parentX + 1;
        final int lithomeY = (nearestCorner & 2) == 0 ? parentY : parentY + 1;
        final int lithomeZ = (nearestCorner & 1) == 0 ? parentZ : parentZ + 1;
        return new Selection(
                lithomeX,
                lithomeY,
                lithomeZ,
                this.noiseLithomeSource.getNoiseLithome(lithomeX, lithomeY, lithomeZ)
        );
    }

    public Holder<Lithome> getNoiseLithomeAtPosition(final BlockPos pos) {
        return this.getNoiseLithomeAtQuart(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ())
        );
    }

    public Holder<Lithome> getNoiseLithomeAtQuart(
            final int quartX,
            final int quartY,
            final int quartZ
    ) {
        return this.noiseLithomeSource.getNoiseLithome(quartX, quartY, quartZ);
    }

    private static double getFiddledDistance(
            final long seed,
            final int randomX,
            final int randomY,
            final int randomZ,
            final double distanceX,
            final double distanceY,
            final double distanceZ
    ) {
        long random = LinearCongruentialGenerator.next(seed, randomX);
        random = LinearCongruentialGenerator.next(random, randomY);
        random = LinearCongruentialGenerator.next(random, randomZ);
        random = LinearCongruentialGenerator.next(random, randomX);
        random = LinearCongruentialGenerator.next(random, randomY);
        random = LinearCongruentialGenerator.next(random, randomZ);
        final double fiddleX = getFiddle(random);
        random = LinearCongruentialGenerator.next(random, seed);
        final double fiddleY = getFiddle(random);
        random = LinearCongruentialGenerator.next(random, seed);
        final double fiddleZ = getFiddle(random);
        return Mth.square(distanceX + fiddleX)
                + Mth.square(distanceY + fiddleY)
                + Mth.square(distanceZ + fiddleZ);
    }

    private static double getFiddle(final long random) {
        final double uniform = (double) Math.floorMod(random >> 24, 1024) / 1024.0D;
        return (uniform - 0.5D) * 0.9D;
    }

    public record Selection(
            int quartX,
            int quartY,
            int quartZ,
            Holder<Lithome> lithome
    ) {
    }

    @FunctionalInterface
    public interface NoiseLithomeSource {
        Holder<Lithome> getNoiseLithome(int quartX, int quartY, int quartZ);
    }
}
