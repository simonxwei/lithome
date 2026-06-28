package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @see net.minecraft.world.level.biome.Biome
 * @author simonxwei
 */
public final class Lithome {

    public static final Codec<Lithome> DIRECT_CODEC;
    public static final Codec<Holder<Lithome>> CODEC;

    private final BlockState defaultBlock;

    public Lithome(final BlockState defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    // public

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    static {
        DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockState.CODEC.fieldOf("default_state").forGetter(Lithome::getDefaultBlock)
        ).apply(i, Lithome::new));
        CODEC = RegistryFileCodec.create(LithomeRegistries.LITHOME, DIRECT_CODEC);
    }
}
