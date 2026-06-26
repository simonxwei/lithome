package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.LithomeMaterialSystem;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSourceParameterList;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSourceParameterLists;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> lithome$overworldSettings =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.withDefaultNamespace("overworld")
            );

    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    @Unique
    private volatile LithomeSource lithome$source;

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
            final @Local(argsOnly = true) StructureManager structureManager
    ) {
        original.call(chunk, biomeResolver, sampler);

        if (!this.lithome$usesOverworldLithomes()) {
            return;
        }

        ((LithomeChunkAccess) (Object) chunk)
                .lithome$fillLithomesFromNoise(
                        this.lithome$getOrCreateSource(structureManager),
                        sampler
                );
    }

    @Unique
    private boolean lithome$usesOverworldLithomes() {
        return this.settings.is(lithome$overworldSettings);
    }

    @Unique
    private LithomeSource lithome$getOrCreateSource(
            final StructureManager structureManager
    ) {
        LithomeSource source = this.lithome$source;
        if (source != null) {
            return source;
        }

        synchronized (this) {
            source = this.lithome$source;
            if (source == null) {
                final Holder<MultiNoiseLithomeSourceParameterList> preset =
                        structureManager
                                .registryAccess()
                                .lookupOrThrow(
                                        LithomeRegistries
                                                .MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST
                                )
                                .getOrThrow(
                                        MultiNoiseLithomeSourceParameterLists.OVERWORLD
                                );

                source = MultiNoiseLithomeSource.createFromPreset(preset);
                this.lithome$source = source;
            }
        }

        return source;
    }

    @Inject(
            method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/world/level/levelgen/blending/Blender;Ljava/util/Set;)V"
            )
    )
    private void lithome$applyMaterialSystem(
            final WorldGenRegion region,
            final StructureManager structureManager,
            final RandomState randomState,
            final ChunkAccess chunk,
            final CallbackInfo ci
    ) {
        if (this.lithome$usesOverworldLithomes()) {
            LithomeMaterialSystem.apply(region, chunk);
        }
    }
}
