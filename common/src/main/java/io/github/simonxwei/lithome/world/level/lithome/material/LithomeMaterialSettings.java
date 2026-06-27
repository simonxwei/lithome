package io.github.simonxwei.lithome.world.level.lithome.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public interface LithomeMaterialSettings {
    Codec<LithomeMaterialSettings> CODEC =
        LithomeBuiltInRegistries.LITHOME_MATERIAL_SETTINGS
            .byNameCodec()
            .dispatchStable(LithomeMaterialSettings::codec, Function.identity());

    MapCodec<? extends LithomeMaterialSettings> codec();

    /**
     * Returns the primary rock used to describe this material configuration in commands.
     * This is derived metadata; Lithome no longer stores a separate base-rock field.
     */
    BlockState baseRock();

    BlockState resolve(LithomeMaterialContext context);
}
