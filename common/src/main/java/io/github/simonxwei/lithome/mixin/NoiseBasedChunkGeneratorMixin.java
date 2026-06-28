package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.levelgen.setting.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.levelgen.setting.LithomeNoiseGeneratorSettingsResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSourceProvider;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeSystem;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
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

import java.util.Optional;
import java.util.Set;

/**
 * @author simonxwei
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements LithomeSourceProvider {

    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    @Unique
    private volatile @Nullable GeneratorSettingsCache lithome$generatorSettingsCache;

    @Unique
    private volatile @Nullable LithomeSamplerCache lithome$samplerCache;

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
        final @Local(argsOnly = true, name = "structureManager") StructureManager structureManager,
        final @Local(argsOnly = true, name = "randomState") RandomState randomState
    ) {
        original.call(chunk, biomeResolver, sampler);
        this.lithome$getGeneratorSettings(structureManager.registryAccess())
            .ifPresent(generatorSettings -> ((LithomeChunkAccess) chunk).lithome$fillLithomesFromNoise(
                    generatorSettings.lithomeSource(),
                    this.lithome$getOrCreateSampler(randomState, sampler)
            ));
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
        this.lithome$getGeneratorSettings(region.registryAccess()).ifPresent(generatorSettings -> LithomeVolumeSystem.apply(
                region,
                protoChunk,
                randomState,
                context,
                this.settings.value().defaultBlock(),
                generatorSettings.volumeRule()
        ));

        original.call(generator, protoChunk, context, randomState, structureManager, biomeManager, blender, possibleBiomes);
    }

    @Unique
    private LithomeClimateSampler lithome$getOrCreateSampler(final RandomState randomState, final Climate.Sampler climateSampler) {
        LithomeSamplerCache cache = this.lithome$samplerCache;
        if (cache != null && cache.randomState() == randomState) {
            return cache.sampler();
        }

        synchronized (this) {
            cache = this.lithome$samplerCache;
            if (cache == null || cache.randomState() != randomState) {
                cache = new LithomeSamplerCache(randomState, LithomeClimateSampler.create(randomState, climateSampler));
                this.lithome$samplerCache = cache;
            }
        }

        return cache.sampler();
    }

    @Unique
    private Optional<LithomeNoiseGeneratorSettings> lithome$getGeneratorSettings(final RegistryAccess registryAccess) {
        GeneratorSettingsCache cache = this.lithome$generatorSettingsCache;
        if (cache != null && cache.registryAccess() == registryAccess) {
            return cache.settings();
        }

        synchronized (this) {
            cache = this.lithome$generatorSettingsCache;
            if (cache == null || cache.registryAccess() != registryAccess) {
                cache = new GeneratorSettingsCache(registryAccess, LithomeNoiseGeneratorSettingsResolver.resolve(registryAccess, this.settings));
                this.lithome$generatorSettingsCache = cache;
            }
        }

        return cache.settings();
    }

    @Override
    public Optional<LithomeSource> lithome$getLithomeSource(final RegistryAccess registryAccess) {
        return this.lithome$getGeneratorSettings(registryAccess).map(LithomeNoiseGeneratorSettings::lithomeSource);
    }

    private record GeneratorSettingsCache(RegistryAccess registryAccess, Optional<LithomeNoiseGeneratorSettings> settings) {}

    private record LithomeSamplerCache(RandomState randomState, LithomeClimateSampler sampler) {}
}
