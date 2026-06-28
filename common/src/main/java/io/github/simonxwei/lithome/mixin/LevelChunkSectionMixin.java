package io.github.simonxwei.lithome.mixin;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkSection;
import io.github.simonxwei.lithome.world.level.chunk.LithomePalettedContainerFactory;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author simonxwei
 */
@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements LithomeChunkSection {

    @Unique
    private PalettedContainerRO<Holder<Lithome>> lithome$lithomes;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/PalettedContainerFactory;)V", at = @At("RETURN"))
    private void lithome$createLithomeContainer(final PalettedContainerFactory containerFactory, final CallbackInfo ci) {
        final LithomePalettedContainerFactory lithomeFactory = (LithomePalettedContainerFactory) (Object) containerFactory;
        if (lithomeFactory.lithome$hasLithomeSupport()) {
            this.lithome$lithomes = lithomeFactory.lithome$createForLithomes();
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunkSection;)V", at = @At("RETURN"))
    private void lithome$copyLithomeContainer(final LevelChunkSection source, final CallbackInfo ci) {
        final LithomeChunkSection lithomeSource = (LithomeChunkSection) (Object) source;
        if (lithomeSource.lithome$hasLithomes()) {
            this.lithome$lithomes = lithomeSource.lithome$getLithomes().copy();
        }
    }

    @Override
    public boolean lithome$hasLithomes() {
        return this.lithome$lithomes != null;
    }

    @Override
    public Holder<Lithome> lithome$getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        return this.lithome$getLithomes().get(quartX, quartY, quartZ);
    }

    @Override
    public void lithome$fillLithomesFromNoise(final LithomeResolver resolver, final LithomeClimateSampler sampler, final int quartMinX, final int quartMinY, final int quartMinZ) {
        final PalettedContainer<Holder<Lithome>> newLithomes = this.lithome$getLithomes().recreate();

        for (int x = 0; x < 4; ++x) {
            for (int y = 0; y < 4; ++y) {
                for (int z = 0; z < 4; ++z) {
                    newLithomes.getAndSetUnchecked(x, y, z, resolver.getNoiseLithome(quartMinX + x, quartMinY + y, quartMinZ + z, sampler)
                    );
                }
            }
        }

        this.lithome$lithomes = newLithomes;
    }

    @Override
    public PalettedContainerRO<Holder<Lithome>> lithome$getLithomes() {
        if (this.lithome$lithomes == null) {
            throw new IllegalStateException(
                    "Lithome container is unavailable for this chunk section"
            );
        }
        return this.lithome$lithomes;
    }

    @Override
    public void lithome$setLithomes(final PalettedContainerRO<Holder<Lithome>> lithomes) {
        if (lithomes == null) {
            throw new IllegalArgumentException("Lithome container cannot be null");
        }
        this.lithome$lithomes = lithomes;
    }
}
