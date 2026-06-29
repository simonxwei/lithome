package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeManager;

import org.jspecify.annotations.Nullable;

/**
 * 主要参考的 Minecraft 原版源码类：
 * - net.minecraft.world.level.biome.BiomeManager
 *
 * Lithome 的普通零散坐标查询继续使用原版式扰动放大；仅显式创建的体积扫描实例
 * 才会委托给 LithomeVolumeSampler。
 *
 * @author simonxwei
 */
public final class LithomeManager {
    private static final long ZOOM_SEED_SALT = 0x4C4954484F4D45L;

    private final NoiseLithomeSource noiseLithomeSource;
    private final long lithomeZoomSeed;
    private final @Nullable LithomeVolumeSampler volumeSampler;

    public LithomeManager(final NoiseLithomeSource noiseLithomeSource, final long seed) {
        this(
            noiseLithomeSource,
            BiomeManager.obfuscateSeed(seed ^ ZOOM_SEED_SALT),
            null
        );
    }

    private LithomeManager(
        final NoiseLithomeSource noiseLithomeSource,
        final long lithomeZoomSeed,
        final @Nullable LithomeVolumeSampler volumeSampler
    ) {
        this.noiseLithomeSource = noiseLithomeSource;
        this.lithomeZoomSeed = lithomeZoomSeed;
        this.volumeSampler = volumeSampler;
    }

    // public

    public LithomeManager withDifferentSource(final NoiseLithomeSource source) {
        if (this.volumeSampler != null) {
            throw new IllegalStateException(
                "A volume-scanning LithomeManager cannot change its source"
            );
        }

        return new LithomeManager(source, this.lithomeZoomSeed, null);
    }

    public LithomeManager forVolumeSampling(
        final int minimumBlockX,
        final int minimumBlockY,
        final int minimumBlockZ,
        final int maximumBlockYExclusive
    ) {
        if (this.volumeSampler != null) {
            throw new IllegalStateException(
                "Lithome volume sampling has already been enabled"
            );
        }

        return new LithomeManager(
            this.noiseLithomeSource,
            this.lithomeZoomSeed,
            new LithomeVolumeSampler(
                this.noiseLithomeSource,
                this.lithomeZoomSeed,
                minimumBlockX,
                minimumBlockY,
                minimumBlockZ,
                maximumBlockYExclusive
            )
        );
    }

    public long volumeSamplerCreatedCells() {
        return this.volumeSampler == null ? 0L : this.volumeSampler.createdCells();
    }

    public long volumeSamplerComputedFiddleOffsets() {
        return this.volumeSampler == null ? 0L : this.volumeSampler.computedFiddleOffsets();
    }

    public Holder<Lithome> getLithome(final BlockPos pos) {
        return this.getLithome(pos.getX(), pos.getY(), pos.getZ());
    }

    public Holder<Lithome> getLithome(
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        if (this.volumeSampler != null) {
            return this.volumeSampler.getLithome(blockX, blockY, blockZ);
        }

        return this.getLithomeSelection(blockX, blockY, blockZ).lithome();
    }

    public Selection getLithomeSelection(final BlockPos pos) {
        return this.getLithomeSelection(pos.getX(), pos.getY(), pos.getZ());
    }

    public Selection getLithomeSelection(
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
            this.noiseLithomeSource.lithome$getNoiseLithome(
                lithomeX,
                lithomeY,
                lithomeZ
            )
        );
    }

    public Holder<Lithome> getNoiseLithomeAtPosition(final BlockPos pos) {
        return this.getNoiseLithomeAtPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public Holder<Lithome> getNoiseLithomeAtPosition(
        final int blockX,
        final int blockY,
        final int blockZ
    ) {
        return this.getNoiseLithomeAtQuart(
            QuartPos.fromBlock(blockX),
            QuartPos.fromBlock(blockY),
            QuartPos.fromBlock(blockZ)
        );
    }

    public Holder<Lithome> getNoiseLithomeAtQuart(
        final int quartX,
        final int quartY,
        final int quartZ
    ) {
        return this.noiseLithomeSource.lithome$getNoiseLithome(
            quartX,
            quartY,
            quartZ
        );
    }

    // core

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
    ) {}

    @FunctionalInterface
    public interface NoiseLithomeSource {
        Holder<Lithome> lithome$getNoiseLithome(
            final int quartX,
            final int quartY,
            final int quartZ
        );
    }
}
