package io.github.simonxwei.lithome.mixin;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSampler;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkMixin implements LithomeChunkAccess {
    @Shadow
    @Final
    private LevelChunk wrapped;

    @Shadow
    @Final
    private boolean allowWrites;

    @Override
    public Holder<Lithome> getNoiseLithome(
            final int quartX,
            final int quartY,
            final int quartZ
    ) {
        return ((LithomeChunkAccess) (Object) this.wrapped)
                .getNoiseLithome(quartX, quartY, quartZ);
    }

    @Override
    public void lithome$fillLithomesFromNoise(
            final LithomeResolver resolver,
            final LithomeSampler sampler
    ) {
        if (this.allowWrites) {
            ((LithomeChunkAccess) (Object) this.wrapped)
                    .lithome$fillLithomesFromNoise(resolver, sampler);
        }
    }
}
