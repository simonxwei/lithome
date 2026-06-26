package io.github.simonxwei.lithome.world.level.lithome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import net.minecraft.core.Holder;

import java.util.function.Function;

public abstract class LithomeSource implements LithomeResolver {

    public static final Codec<LithomeSource> CODEC;

    protected LithomeSource() {}

    // public

    protected abstract MapCodec<? extends LithomeSource> codec();

    // lithome resolver interface

    @Override
    public abstract Holder<Lithome> getNoiseLithome(final int quartX, final int quartY, final int quartZ);

    static {
        CODEC = LithomeBuiltInRegistries.LITHOME_SOURCE.byNameCodec().dispatchStable(LithomeSource::codec, Function.identity());
    }
}
