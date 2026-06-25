package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.simonxwei.lithome.data.worldgen.lithome.OverworldLithomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

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
        return OverworldLithomes.andesite().getBaseRock();
    }
}
