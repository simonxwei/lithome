package io.github.simonxwei.lithome.world.level.levelgen.surface.condition.custom;

import io.github.simonxwei.lithome.mixin.SurfaceRulesContextAccessor;
import io.github.simonxwei.lithome.world.level.levelgen.surface.condition.LithomeLazyXZCondition;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * @see net.minecraft.world.level.levelgen.SurfaceRules.Context.SteepMaterialCondition
 * @author simonxwei
 */
public final class LithomeSteepCondition extends LithomeLazyXZCondition {

    private final int height;

    public LithomeSteepCondition(final SurfaceRules.Context context, final int height) {
        super(context);
        this.height = height;
    }

    // core

    @Override
    protected boolean compute() {
        final SurfaceRulesContextAccessor accessor = (SurfaceRulesContextAccessor) (Object) this.context;
        final ChunkAccess chunk = accessor.lithome$getChunk();
        final int chunkBlockX = accessor.lithome$getBlockX() & 15;
        final int chunkBlockZ = accessor.lithome$getBlockZ() & 15;
        final int zNorth = Math.max(chunkBlockZ - 1, 0);
        final int zSouth = Math.min(chunkBlockZ + 1, 15);
        final int heightNorth = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkBlockX, zNorth);
        final int heightSouth = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkBlockX, zSouth);
        if (Mth.abs(heightNorth - heightSouth) >= this.height) return true;
        final int xWest = Math.max(chunkBlockX - 1, 0);
        final int xEast = Math.min(chunkBlockX + 1, 15);
        final int heightWest = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xWest, chunkBlockZ);
        final int heightEast = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xEast, chunkBlockZ);
        return Mth.abs(heightWest - heightEast) >= this.height;
    }
}
