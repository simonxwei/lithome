package io.github.simonxwei.lithome.world.level.levelgen.surface.rule;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.mixin.SurfaceRulesContextAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record Skip() implements SurfaceRules.RuleSource {

    public static final MapCodec<Skip> CODEC;

    // rule source interface

    @Override
    public MapCodec<Skip> codec() {
        return CODEC;
    }

    // core

    @Override
    public SurfaceRules.SurfaceRule apply(final SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor accessor = (SurfaceRulesContextAccessor) (Object) context;
        final ChunkAccess chunk = accessor.getChunk();
        final BlockPos.MutableBlockPos pos = accessor.getPos();
        return (blockX, blockY, blockZ) -> chunk.getBlockState(pos.set(blockX, blockY, blockZ));
    }

    static {
        CODEC = MapCodec.unit(Skip::new);
    }
}
