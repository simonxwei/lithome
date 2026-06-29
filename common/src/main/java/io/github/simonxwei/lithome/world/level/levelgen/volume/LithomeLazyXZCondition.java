package io.github.simonxwei.lithome.world.level.levelgen.volume;

/**
 * 主要参考 Minecraft 原版 SurfaceRules.LazyXZCondition，
 * 并参考 Lithome 已有的表面规则 LithomeLazyXZCondition。
 *
 * @author simonxwei
 */
public abstract class LithomeLazyXZCondition extends LithomeLazyCondition {
    public LithomeLazyXZCondition(final LithomeVolumeRules.Context context) {
        super(context);
    }

    // core

    @Override
    protected final long getContextUpdateId() {
        return this.context.xzUpdateId();
    }
}
