package io.github.simonxwei.lithome.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author simonxwei
 */
@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {

    @Accessor("chunk")
    ChunkAccess lithome$getChunk();

    @Accessor("lastUpdateXZ")
    long lithome$getLastUpdateXZ();

    @Accessor("blockX")
    int lithome$getBlockX();

    @Accessor("blockZ")
    int lithome$getBlockZ();

    @Accessor("pos")
    BlockPos.MutableBlockPos lithome$getPos();
}
