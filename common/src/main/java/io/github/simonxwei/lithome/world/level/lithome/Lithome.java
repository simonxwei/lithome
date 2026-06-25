package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.block.state.BlockState;

public final class Lithome {

    public static final Codec<Lithome> DIRECT_CODEC;
    public static final Codec<Holder<Lithome>> CODEC;

    private final BlockState baseRock;

    public Lithome(final BlockState baseRock) {
        this.baseRock = baseRock;
    }

    public BlockState getBaseRock() {
        return this.baseRock;
    }

    static {
        DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockState.CODEC.fieldOf("base_rock").forGetter(Lithome::getBaseRock)
        ).apply(i, Lithome::new));
        CODEC = RegistryFileCodec.create(LithomeRegistries.LITHOME, DIRECT_CODEC);
    }
}
