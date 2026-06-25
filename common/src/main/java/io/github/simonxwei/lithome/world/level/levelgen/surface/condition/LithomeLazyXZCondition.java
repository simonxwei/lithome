package io.github.simonxwei.lithome.world.level.levelgen.surface.condition;

import io.github.simonxwei.lithome.mixin.SurfaceRulesContextAccessor;
import net.minecraft.world.level.levelgen.SurfaceRules;

public abstract class LithomeLazyXZCondition extends LithomeLazyCondition {

    public LithomeLazyXZCondition(final SurfaceRules.Context context) {
        super(context);
    }

    // core

    @Override
    protected long getContextLastUpdate() {
        return ((SurfaceRulesContextAccessor) (Object) this.context).lithome$getLastUpdateXZ();
    }
}
