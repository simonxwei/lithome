package io.github.simonxwei.lithome.world.level.lithome;

import net.minecraft.core.Holder;

@FunctionalInterface
public interface LithomeResolver {
    Holder<Lithome> getNoiseLithome(
            int quartX,
            int quartY,
            int quartZ,
            LithomeSampler sampler
    );
}
