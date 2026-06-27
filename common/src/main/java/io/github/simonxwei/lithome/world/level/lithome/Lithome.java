package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.lithome.material.LithomeMaterialSettings;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public final class Lithome {
    public static final Codec<Lithome> DIRECT_CODEC;
    public static final Codec<Holder<Lithome>> CODEC;

    private final LithomeMaterialSettings materials;

    public Lithome(final LithomeMaterialSettings materials) {
        this.materials = materials;
    }

    public LithomeMaterialSettings getMaterials() {
        return this.materials;
    }

    /**
     * Returns the primary rock for command output and other descriptive uses.
     * The value is derived from the material settings and is not stored separately.
     */
    public net.minecraft.world.level.block.state.BlockState getBaseRock() {
        return this.materials.baseRock();
    }

    static {
        DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LithomeMaterialSettings.CODEC
                .fieldOf("materials")
                .forGetter(Lithome::getMaterials)
        ).apply(instance, Lithome::new));
        CODEC = RegistryFileCodec.create(LithomeRegistries.LITHOME, DIRECT_CODEC);
    }
}
