package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.lithome.FixedLithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.Lithomes;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Unique
    private volatile LithomeSource lithome$lithomeSource;

    @Inject(method = "fillFromNoise", at = @At("HEAD"))
    private void lithome$prepareLithomeSource(
            final Blender blender,
            final RandomState randomState,
            final StructureManager structureManager,
            final ChunkAccess centerChunk,
            final CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (this.lithome$lithomeSource != null) return;

        final Holder<Lithome> andesite = structureManager
                .registryAccess()
                .lookupOrThrow(LithomeRegistries.LITHOME)
                .getOrThrow(Lithomes.ANDESITE);

        this.lithome$lithomeSource = new FixedLithomeSource(andesite);
    }

    @ModifyReturnValue(method = "debugPreliminarySurfaceLevel", at = @At("RETURN"))
    private BlockState lithome$debugPreliminarySurfaceLevelMixin(
            final BlockState original,
            final NoiseChunk noiseChunk,
            final int posX,
            final int posY,
            final int posZ,
            final BlockState state
    ) {
        if (!original.is(Blocks.STONE)) return original;

        final LithomeSource lithomeSource = this.lithome$lithomeSource;

        if (lithomeSource == null) {
            throw new IllegalStateException("Lithome source was not initialized before terrain filling");
        }

        return lithomeSource
                .getNoiseLithome(
                        QuartPos.fromBlock(posX),
                        QuartPos.fromBlock(posY),
                        QuartPos.fromBlock(posZ)
                )
                .value()
                .getBaseRock();
    }
}
