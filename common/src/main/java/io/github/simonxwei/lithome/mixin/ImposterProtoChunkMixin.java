package io.github.simonxwei.lithome.mixin;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author simonxwei
 */
@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkMixin implements LithomeChunkAccess {

    @Shadow
    @Final
    private LevelChunk wrapped;

    @Shadow
    @Final
    private boolean allowWrites;

    @Override
    public Holder<Lithome> lithome$getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return ((LithomeChunkAccess) this.wrapped).lithome$getNoiseLithome(quartX, quartY, quartZ);
    }

    @Override
    public void lithome$fillLithomesFromNoise(final LithomeResolver resolver, final LithomeClimateSampler sampler) {
        if (this.allowWrites) {
            ((LithomeChunkAccess) this.wrapped).lithome$fillLithomesFromNoise(resolver, sampler);
        }
    }
}
