package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;

import java.util.function.Function;

public abstract class LithomeSource implements LithomeResolver {

    public static final Codec<LithomeSource> CODEC;

    protected LithomeSource() {
    }

    protected abstract MapCodec<? extends LithomeSource> codec();

    @Override
    public abstract Holder<Lithome> getNoiseLithome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler
    );

    static {
        CODEC = LithomeBuiltInRegistries.LITHOME_SOURCE
                .byNameCodec()
                .dispatchStable(LithomeSource::codec, Function.identity());
    }
}
