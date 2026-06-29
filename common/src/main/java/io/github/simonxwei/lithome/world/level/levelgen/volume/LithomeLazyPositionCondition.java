package io.github.simonxwei.lithome.world.level.levelgen.volume;

/**
 * 主要参考 Minecraft 原版 SurfaceRules.LazyYCondition。
 * 与原版按 Y 更新不同，本类按 Lithome 体积规则的完整候选位置更新。
 *
 * @author simonxwei
 */
public abstract class LithomeLazyPositionCondition extends LithomeLazyCondition {
    public LithomeLazyPositionCondition(final LithomeVolumeRules.Context context) {
        super(context);
    }

    // core

    @Override
    protected final long getContextUpdateId() {
        return this.context.positionUpdateId();
    }
}
