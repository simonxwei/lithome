package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.simonxwei.lithome.data.worldgen.lithome.OverworldLithomes;
import io.github.simonxwei.lithome.world.level.lithome.FixedLithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Unique
    private static final LithomeSource lithome$andesiteSource;

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
        return lithome$andesiteSource.getNoiseLithome(
                QuartPos.fromBlock(posX),
                QuartPos.fromBlock(posY),
                QuartPos.fromBlock(posZ)
        ).getBaseRock();
    }

    static {
        lithome$andesiteSource = new FixedLithomeSource(OverworldLithomes.andesite());
    }
}
