package io.github.simonxwei.lithome.world.level.levelgen;

import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import net.minecraft.core.Holder;

/**
 * @author simonxwei
 */
public interface LithomeNoiseBasedChunkGeneratorExtension {

    void lithome$configure(final LithomeSource lithomeSource, final Holder<LithomeNoiseGeneratorSettings> noiseSettings);

    boolean lithome$isConfigured();

    LithomeSource lithome$getConfiguredLithomeSource();

    Holder<LithomeNoiseGeneratorSettings> lithome$getConfiguredNoiseSettings();
}
