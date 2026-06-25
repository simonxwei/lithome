package io.github.simonxwei.lithome.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {

    @Accessor("chunk")
    ChunkAccess getChunk();

    @Accessor("lastUpdateXZ")
    long getLastUpdateXZ();

    @Accessor("blockX")
    int getBlockX();

    @Accessor("blockZ")
    int getBlockZ();

    @Accessor("pos")
    BlockPos.MutableBlockPos getPos();
}
