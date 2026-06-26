package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;

@FunctionalInterface
public interface LithomeResolver {

    Holder<Lithome> getNoiseLithome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler
    );
}
