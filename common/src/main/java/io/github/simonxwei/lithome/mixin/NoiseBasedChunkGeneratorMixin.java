package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.mojang.serialization.MapCodec;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkGenerators;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseBasedChunkGeneratorExtension;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.levelgen.performance.LithomeWorldgenPerformance;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeSystem;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSourceProvider;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;

import net.minecraft.server.level.WorldGenRegion;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

/**
 * @see NoiseBasedChunkGenerator
 * @author simonxwei
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements
    LithomeSourceProvider,
    LithomeNoiseBasedChunkGeneratorExtension {
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    @Unique
    private volatile @Nullable LithomeSource lithome$source;
    @Unique
    private volatile @Nullable Holder<LithomeNoiseGeneratorSettings> lithome$noiseSettings;
    @Unique
    private volatile @Nullable LithomeSamplerCache lithome$samplerCache;

    @Inject(
        method = "codec",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lithome$useConfiguredCodec(
        final CallbackInfoReturnable<MapCodec<? extends ChunkGenerator>> cir
    ) {
        if (this.lithome$isConfigured()) {
            cir.setReturnValue(LithomeChunkGenerators.CODEC);
        }
    }

    @WrapOperation(
        method = "doCreateBiomes",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkAccess;fillBiomesFromNoise(Lnet/minecraft/world/level/biome/BiomeResolver;Lnet/minecraft/world/level/biome/Climate$Sampler;)V"
        )
    )
    private void lithome$fillLithomesFromNoise(
        final ChunkAccess chunk,
        final BiomeResolver biomeResolver,
        final Climate.Sampler sampler,
        final Operation<Void> original,
        final @Local(argsOnly = true, name = "randomState") RandomState randomState
    ) {
        original.call(chunk, biomeResolver, sampler);

        if (!this.lithome$isConfigured()) {
            return;
        }

        final long performanceStartedAt = LithomeWorldgenPerformance.beginBiomes();
        try {
            ((LithomeChunkAccess) chunk).lithome$fillLithomesFromNoise(
                this.lithome$getConfiguredLithomeSource(),
                this.lithome$getOrCreateSampler(randomState, sampler)
            );
        } finally {
            LithomeWorldgenPerformance.finishBiomes(chunk.getPos(), performanceStartedAt);
        }
    }

    @WrapOperation(
        method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/world/level/levelgen/blending/Blender;Ljava/util/Set;)V"
        )
    )
    private void lithome$applyVolumeRulesBeforeSurface(
        final NoiseBasedChunkGenerator generator,
        final ChunkAccess protoChunk,
        final WorldGenerationContext context,
        final RandomState randomState,
        final StructureManager structureManager,
        final BiomeManager biomeManager,
        final Blender blender,
        final @Nullable Set<Holder<Biome>> possibleBiomes,
        final Operation<Void> original,
        final @Local(argsOnly = true, name = "region") WorldGenRegion region
    ) {
        if (this.lithome$isConfigured()) {
            LithomeVolumeSystem.apply(
                region,
                protoChunk,
                randomState,
                context,
                this.settings.value().defaultBlock(),
                this.lithome$getConfiguredNoiseSettings().value().volumeRule()
            );
        }

        original.call(
            generator,
            protoChunk,
            context,
            randomState,
            structureManager,
            biomeManager,
            blender,
            possibleBiomes
        );
    }

    // extension

    @Override
    public synchronized void lithome$configure(
        final LithomeSource lithomeSource,
        final Holder<LithomeNoiseGeneratorSettings> noiseSettings
    ) {
        if (this.lithome$source != null || this.lithome$noiseSettings != null) {
            throw new IllegalStateException("Lithome settings have already been configured for this NoiseBasedChunkGenerator");
        }

        this.lithome$source = lithomeSource;
        this.lithome$noiseSettings = noiseSettings;
    }

    @Override
    public boolean lithome$isConfigured() {
        return this.lithome$source != null && this.lithome$noiseSettings != null;
    }

    @Override
    public LithomeSource lithome$getConfiguredLithomeSource() {
        final LithomeSource lithomeSource = this.lithome$source;
        if (lithomeSource == null) {
            throw new IllegalStateException("NoiseBasedChunkGenerator has no configured LithomeSource");
        }

        return lithomeSource;
    }

    @Override
    public Holder<LithomeNoiseGeneratorSettings> lithome$getConfiguredNoiseSettings() {
        final Holder<LithomeNoiseGeneratorSettings> noiseSettings = this.lithome$noiseSettings;
        if (noiseSettings == null) {
            throw new IllegalStateException("NoiseBasedChunkGenerator has no configured Lithome noise settings");
        }

        return noiseSettings;
    }

    @Override
    public Optional<LithomeSource> lithome$getLithomeSource(final RegistryAccess registryAccess) {
        return Optional.ofNullable(this.lithome$source);
    }

    // core

    @Unique
    private LithomeClimateSampler lithome$getOrCreateSampler(
        final RandomState randomState,
        final Climate.Sampler climateSampler
    ) {
        LithomeSamplerCache cache = this.lithome$samplerCache;
        if (cache != null && cache.randomState() == randomState) {
            return cache.sampler();
        }

        synchronized (this) {
            cache = this.lithome$samplerCache;
            if (cache == null || cache.randomState() != randomState) {
                cache = new LithomeSamplerCache(
                    randomState,
                    LithomeClimateSampler.create(randomState, climateSampler)
                );
                this.lithome$samplerCache = cache;
            }
        }

        return cache.sampler();
    }

    private record LithomeSamplerCache(
        RandomState randomState,
        LithomeClimateSampler sampler
    ) {}
}
