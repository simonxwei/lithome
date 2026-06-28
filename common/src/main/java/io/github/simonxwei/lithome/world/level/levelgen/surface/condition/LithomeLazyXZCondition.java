package io.github.simonxwei.lithome.world.level.levelgen.surface.condition;

import io.github.simonxwei.lithome.mixin.SurfaceRulesContextAccessor;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * @see net.minecraft.world.level.levelgen.SurfaceRules.LazyXZCondition
 * @author simonxwei
 */
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
