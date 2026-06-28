package io.github.simonxwei.lithome.world.level.levelgen.setting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * @see NoiseGeneratorSettings
 * @author simonxwei
 */
public record LithomeNoiseGeneratorSettings(LithomeSource lithomeSource, LithomeVolumeRules.RuleSource volumeRule) {

    public static final Codec<LithomeNoiseGeneratorSettings> DIRECT_CODEC;
    public static final Codec<Holder<LithomeNoiseGeneratorSettings>> CODEC;

    static {
        DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
                LithomeSource.CODEC.fieldOf("lithome_source").forGetter(LithomeNoiseGeneratorSettings::lithomeSource),
                LithomeVolumeRules.RuleSource.CODEC.fieldOf("volume_rule").forGetter(LithomeNoiseGeneratorSettings::volumeRule)
        ).apply(i, LithomeNoiseGeneratorSettings::new));
        CODEC = RegistryFileCodec.create(LithomeRegistries.NOISE_SETTINGS, DIRECT_CODEC);
    }
}
