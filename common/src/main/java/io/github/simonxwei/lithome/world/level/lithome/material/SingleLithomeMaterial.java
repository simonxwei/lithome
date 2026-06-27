package io.github.simonxwei.lithome.world.level.lithome.material;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public record SingleLithomeMaterial(BlockState state) implements LithomeMaterialSettings {
    public static final MapCodec<SingleLithomeMaterial> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(SingleLithomeMaterial::state)
        ).apply(instance, SingleLithomeMaterial::new));

    @Override
    public MapCodec<? extends LithomeMaterialSettings> codec() {
        return CODEC;
    }

    @Override
    public BlockState baseRock() {
        return this.state;
    }

    @Override
    public BlockState resolve(final LithomeMaterialContext context) {
        return this.state;
    }
}
