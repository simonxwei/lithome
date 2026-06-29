package io.github.simonxwei.lithome.world.level.levelgen.volume;

/**
 * 主要参考 Minecraft 原版 SurfaceRules.LazyCondition，
 * 并参考 Lithome 已有的表面规则 LithomeLazyCondition。
 *
 * @author simonxwei
 */
public abstract class LithomeLazyCondition implements LithomeVolumeRules.VolumeCondition {
    protected final LithomeVolumeRules.Context context;

    private boolean initialized;
    private boolean result;
    private long lastUpdate;

    public LithomeLazyCondition(final LithomeVolumeRules.Context context) {
        this.context = context;
    }

    // public

    @Override
    public final boolean test() {
        final long contextUpdate = this.getContextUpdateId();
        if (!this.initialized || contextUpdate != this.lastUpdate) {
            this.result = this.compute();
            this.lastUpdate = contextUpdate;
            this.initialized = true;
        }
        return this.result;
    }

    // core

    protected abstract boolean compute();

    protected abstract long getContextUpdateId();
}
