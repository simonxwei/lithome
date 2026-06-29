package io.github.simonxwei.lithome.world.level.levelgen.performance;

import io.github.simonxwei.lithome.Constants;

import net.minecraft.world.level.ChunkPos;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * 无直接对应的 Minecraft 原版类。
 *
 * 相关参考类：
 * - net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
 * - net.minecraft.world.level.levelgen.SurfaceSystem
 *
 * 本类只在显式启用时收集 Lithome 世界生成统计，并按区块窗口低频汇总输出。
 *
 * @author simonxwei
 */
public final class LithomeWorldgenPerformance {
    private static final String ENABLED_PROPERTY = "lithome.performance.enabled";
    private static final String WINDOW_PROPERTY = "lithome.performance.window";

    private static final boolean ENABLED = Boolean.getBoolean(ENABLED_PROPERTY);
    private static final int WINDOW_SIZE = Math.max(
        1,
        Integer.getInteger(WINDOW_PROPERTY, 256)
    );

    private static final Accumulator ACCUMULATOR = new Accumulator();

    static {
        if (ENABLED) {
            Constants.LOGGER.info(
                "Lithome performance measurement enabled; reporting every {} volume chunks",
                WINDOW_SIZE
            );
        }
    }

    private LithomeWorldgenPerformance() {}

    // public

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static long beginBiomes() {
        return ENABLED ? System.nanoTime() : 0L;
    }

    public static void finishBiomes(
        final ChunkPos chunkPos,
        final long startedAtNanos
    ) {
        if (!ENABLED) {
            return;
        }

        ACCUMULATOR.recordBiomes(
            chunkPos,
            System.nanoTime() - startedAtNanos
        );
    }

    public static @Nullable VolumeSample beginVolume(final ChunkPos chunkPos) {
        return ENABLED ? new VolumeSample(chunkPos, System.nanoTime()) : null;
    }

    public static void finishVolume(final @Nullable VolumeSample sample) {
        if (sample == null) {
            return;
        }

        final Report report = ACCUMULATOR.recordVolume(
            sample,
            System.nanoTime() - sample.startedAtNanos
        );

        if (report != null) {
            report.log();
        }
    }

    public static final class VolumeSample {
        private final ChunkPos chunkPos;
        private final long startedAtNanos;

        private long sectionsScanned;
        private long sectionsSkipped;
        private long blocksChecked;
        private long candidateBlocks;
        private long lithomeQueries;
        private long volumeSamplerCells;
        private long fiddleOffsetComputations;
        private long noiseSamples2d;
        private long noiseSamples3d;
        private long changedBlocks;
        private long paletteReads;
        private long sourceChunkCacheMisses;

        private VolumeSample(
            final ChunkPos chunkPos,
            final long startedAtNanos
        ) {
            this.chunkPos = chunkPos;
            this.startedAtNanos = startedAtNanos;
        }

        public void incrementSectionsScanned() {
            ++this.sectionsScanned;
        }

        public void incrementSectionsSkipped() {
            ++this.sectionsSkipped;
        }

        public void incrementBlocksChecked() {
            ++this.blocksChecked;
        }

        public void incrementCandidateBlocks() {
            ++this.candidateBlocks;
        }

        public void incrementLithomeQueries() {
            ++this.lithomeQueries;
        }

        public void addVolumeSamplerCells(final long count) {
            this.volumeSamplerCells += count;
        }

        public void addFiddleOffsetComputations(final long count) {
            this.fiddleOffsetComputations += count;
        }

        public void incrementNoiseSamples2d() {
            ++this.noiseSamples2d;
        }

        public void incrementNoiseSamples3d() {
            ++this.noiseSamples3d;
        }

        public void incrementChangedBlocks() {
            ++this.changedBlocks;
        }

        public void incrementPaletteReads() {
            ++this.paletteReads;
        }

        public void incrementSourceChunkCacheMisses() {
            ++this.sourceChunkCacheMisses;
        }
    }

    // core

    private static final class Accumulator {
        private long biomesChunks;
        private long biomesNanos;
        private long slowestBiomesNanos;
        private @Nullable ChunkPos slowestBiomesChunk;

        private long volumeChunks;
        private long volumeNanos;
        private long slowestVolumeNanos;
        private @Nullable ChunkPos slowestVolumeChunk;

        private long sectionsScanned;
        private long sectionsSkipped;
        private long blocksChecked;
        private long candidateBlocks;
        private long lithomeQueries;
        private long volumeSamplerCells;
        private long fiddleOffsetComputations;
        private long noiseSamples2d;
        private long noiseSamples3d;
        private long changedBlocks;
        private long paletteReads;
        private long sourceChunkCacheMisses;

        public synchronized void recordBiomes(
            final ChunkPos chunkPos,
            final long elapsedNanos
        ) {
            ++this.biomesChunks;
            this.biomesNanos += elapsedNanos;

            if (elapsedNanos > this.slowestBiomesNanos) {
                this.slowestBiomesNanos = elapsedNanos;
                this.slowestBiomesChunk = chunkPos;
            }
        }

        public synchronized @Nullable Report recordVolume(
            final VolumeSample sample,
            final long elapsedNanos
        ) {
            ++this.volumeChunks;
            this.volumeNanos += elapsedNanos;

            if (elapsedNanos > this.slowestVolumeNanos) {
                this.slowestVolumeNanos = elapsedNanos;
                this.slowestVolumeChunk = sample.chunkPos;
            }

            this.sectionsScanned += sample.sectionsScanned;
            this.sectionsSkipped += sample.sectionsSkipped;
            this.blocksChecked += sample.blocksChecked;
            this.candidateBlocks += sample.candidateBlocks;
            this.lithomeQueries += sample.lithomeQueries;
            this.volumeSamplerCells += sample.volumeSamplerCells;
            this.fiddleOffsetComputations += sample.fiddleOffsetComputations;
            this.noiseSamples2d += sample.noiseSamples2d;
            this.noiseSamples3d += sample.noiseSamples3d;
            this.changedBlocks += sample.changedBlocks;
            this.paletteReads += sample.paletteReads;
            this.sourceChunkCacheMisses += sample.sourceChunkCacheMisses;

            if (this.volumeChunks < WINDOW_SIZE) {
                return null;
            }

            final Report report = new Report(
                this.biomesChunks,
                this.biomesNanos,
                this.slowestBiomesNanos,
                this.slowestBiomesChunk,
                this.volumeChunks,
                this.volumeNanos,
                this.slowestVolumeNanos,
                this.slowestVolumeChunk,
                this.sectionsScanned,
                this.sectionsSkipped,
                this.blocksChecked,
                this.candidateBlocks,
                this.lithomeQueries,
                this.volumeSamplerCells,
                this.fiddleOffsetComputations,
                this.noiseSamples2d,
                this.noiseSamples3d,
                this.changedBlocks,
                this.paletteReads,
                this.sourceChunkCacheMisses
            );

            this.reset();
            return report;
        }

        private void reset() {
            this.biomesChunks = 0L;
            this.biomesNanos = 0L;
            this.slowestBiomesNanos = 0L;
            this.slowestBiomesChunk = null;

            this.volumeChunks = 0L;
            this.volumeNanos = 0L;
            this.slowestVolumeNanos = 0L;
            this.slowestVolumeChunk = null;

            this.sectionsScanned = 0L;
            this.sectionsSkipped = 0L;
            this.blocksChecked = 0L;
            this.candidateBlocks = 0L;
            this.lithomeQueries = 0L;
            this.volumeSamplerCells = 0L;
            this.fiddleOffsetComputations = 0L;
            this.noiseSamples2d = 0L;
            this.noiseSamples3d = 0L;
            this.changedBlocks = 0L;
            this.paletteReads = 0L;
            this.sourceChunkCacheMisses = 0L;
        }
    }

    private record Report(
        long biomesChunks,
        long biomesNanos,
        long slowestBiomesNanos,
        @Nullable ChunkPos slowestBiomesChunk,
        long volumeChunks,
        long volumeNanos,
        long slowestVolumeNanos,
        @Nullable ChunkPos slowestVolumeChunk,
        long sectionsScanned,
        long sectionsSkipped,
        long blocksChecked,
        long candidateBlocks,
        long lithomeQueries,
        long volumeSamplerCells,
        long fiddleOffsetComputations,
        long noiseSamples2d,
        long noiseSamples3d,
        long changedBlocks,
        long paletteReads,
        long sourceChunkCacheMisses
    ) {
        private void log() {
            Constants.LOGGER.info(
                "Lithome performance ({} volume chunks): "
                    + "BIOMES chunks={}, total={} ms, avg={} ms, slowest={} ms at {}; "
                    + "VOLUME total={} ms, avg={} ms, slowest={} ms at {}; "
                    + "sections scanned={}, skipped={}; blocks checked={}, candidates={}; "
                    + "Lithome queries={}, distance evaluations={}, sampler cells={}, "
                    + "fiddle offsets={}; noise samples 2D={}, 3D={}; "
                    + "changed blocks={}, palette reads={}, source-chunk cache misses={}",
                this.volumeChunks,
                this.biomesChunks,
                formatMillis(this.biomesNanos),
                formatAverageMillis(this.biomesNanos, this.biomesChunks),
                formatMillis(this.slowestBiomesNanos),
                formatChunk(this.slowestBiomesChunk),
                formatMillis(this.volumeNanos),
                formatAverageMillis(this.volumeNanos, this.volumeChunks),
                formatMillis(this.slowestVolumeNanos),
                formatChunk(this.slowestVolumeChunk),
                this.sectionsScanned,
                this.sectionsSkipped,
                this.blocksChecked,
                this.candidateBlocks,
                this.lithomeQueries,
                this.lithomeQueries * 8L,
                this.volumeSamplerCells,
                this.fiddleOffsetComputations,
                this.noiseSamples2d,
                this.noiseSamples3d,
                this.changedBlocks,
                this.paletteReads,
                this.sourceChunkCacheMisses
            );
        }
    }

    private static String formatMillis(final long nanos) {
        return String.format(Locale.ROOT, "%.3f", nanos / 1_000_000.0D);
    }

    private static String formatAverageMillis(
        final long nanos,
        final long count
    ) {
        if (count == 0L) {
            return "0.000";
        }

        return String.format(
            Locale.ROOT,
            "%.3f",
            nanos / (count * 1_000_000.0D)
        );
    }

    private static String formatChunk(final @Nullable ChunkPos chunkPos) {
        return chunkPos == null ? "n/a" : chunkPos.toString();
    }
}
