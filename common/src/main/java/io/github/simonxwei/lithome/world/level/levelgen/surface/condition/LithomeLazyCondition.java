package io.github.simonxwei.lithome.world.level.levelgen.surface.condition;

import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jspecify.annotations.Nullable;

public abstract class LithomeLazyCondition implements SurfaceRules.Condition {

    protected final SurfaceRules.Context context;

    @Nullable
    private Boolean result;

    private long lastUpdate;

    public LithomeLazyCondition(final SurfaceRules.Context context) {
        this.context = context;
        this.lastUpdate = this.getContextLastUpdate() - 1L;
    }

    // public

    @Override
    public boolean test() {
        final long lastContextUpdate = this.getContextLastUpdate();
        if (lastContextUpdate == this.lastUpdate) {
            if (this.result == null) {
                throw new IllegalStateException("Update triggered but the result is null");
            }
            return this.result;
        }
        this.result = this.compute();
        this.lastUpdate = lastContextUpdate;
        return this.result;
    }

    // core

    protected abstract boolean compute();

    protected abstract long getContextLastUpdate();
}
