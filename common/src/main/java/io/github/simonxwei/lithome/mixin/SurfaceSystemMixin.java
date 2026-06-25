package io.github.simonxwei.lithome.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.simonxwei.lithome.tags.LithomeBlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin {

    @WrapOperation(
            method = "buildSurface",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/levelgen/SurfaceSystem;defaultBlock:Lnet/minecraft/world/level/block/state/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState erosion$wrapDefaultBlockCheck(final SurfaceSystem instance, final Operation<BlockState> original, final @Local(name = "old") BlockState old) {
        return old.is(LithomeBlockTags.BASE_STONE_OVERWORLD) ? old : original.call(instance);
    }
}
