package io.github.simonxwei.lithome.mixin;

import com.mojang.serialization.Codec;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.chunk.LithomePalettedContainerFactory;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.Lithomes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PalettedContainerFactory.class)
public abstract class PalettedContainerFactoryMixin implements LithomePalettedContainerFactory {

    @Unique
    private boolean lithome$lithomeInitializationAttempted;

    @Unique
    private Strategy<Holder<Lithome>> lithome$lithomeStrategy;

    @Unique
    private Holder<Lithome> lithome$defaultLithome;

    @Unique
    private Codec<PalettedContainerRO<Holder<Lithome>>> lithome$lithomeContainerCodec;

    @Inject(method = "create", at = @At("RETURN"))
    private static void lithome$initializeCreatedFactory(
            final RegistryAccess registries,
            final CallbackInfoReturnable<PalettedContainerFactory> cir
    ) {
        ((LithomePalettedContainerFactory) (Object) cir.getReturnValue())
                .lithome$initializeLithomes(registries);
    }

    @Override
    public void lithome$initializeLithomes(final RegistryAccess registries) {
        if (this.lithome$lithomeInitializationAttempted) {
            return;
        }
        this.lithome$lithomeInitializationAttempted = true;

        final Registry<Lithome> lithomeRegistry;
        try {
            lithomeRegistry = registries.lookupOrThrow(LithomeRegistries.LITHOME);
        } catch (final IllegalStateException missingRegistry) {
            // The first implementation deliberately does not synchronize Lithome identities
            // to the client. ClientLevel therefore has no Lithome dynamic registry yet.
            return;
        }

        this.lithome$lithomeStrategy = Strategy.createForBiomes(lithomeRegistry.asHolderIdMap());
        this.lithome$defaultLithome = lithomeRegistry.getOrThrow(Lithomes.STONE);
        this.lithome$lithomeContainerCodec = PalettedContainer.codecRO(
                lithomeRegistry.holderByNameCodec(),
                this.lithome$lithomeStrategy,
                this.lithome$defaultLithome
        );
    }

    @Override
    public boolean lithome$hasLithomeSupport() {
        return this.lithome$lithomeStrategy != null
                && this.lithome$defaultLithome != null
                && this.lithome$lithomeContainerCodec != null;
    }

    @Override
    public PalettedContainer<Holder<Lithome>> lithome$createForLithomes() {
        this.lithome$checkInitialized();
        return new PalettedContainer<>(this.lithome$defaultLithome, this.lithome$lithomeStrategy);
    }

    @Override
    public Codec<PalettedContainerRO<Holder<Lithome>>> lithome$lithomeContainerCodec() {
        this.lithome$checkInitialized();
        return this.lithome$lithomeContainerCodec;
    }

    @Unique
    private void lithome$checkInitialized() {
        if (!this.lithome$hasLithomeSupport()) {
            throw new IllegalStateException(
                    "Lithome palette support is unavailable for this registry access"
            );
        }
    }
}
