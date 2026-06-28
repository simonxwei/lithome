package io.github.simonxwei.lithome.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * @see NoiseGeneratorSettings
 * @author simonxwei
 */
public record LithomeNoiseGeneratorSettings(
    LithomeVolumeRules.RuleSource volumeRule
) {

    public static final ResourceKey<LithomeNoiseGeneratorSettings> OVERWORLD;

    public static final Codec<LithomeNoiseGeneratorSettings> DIRECT_CODEC;
    public static final Codec<Holder<LithomeNoiseGeneratorSettings>> CODEC;

    // core

    private static ResourceKey<LithomeNoiseGeneratorSettings> register(final String name) {
        return ResourceKey.create(LithomeRegistries.NOISE_SETTINGS, Constants.id(name));
    }

    static {
        OVERWORLD = register("overworld");

        DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            LithomeVolumeRules.RuleSource.CODEC
                .fieldOf("volume_rule")
                .forGetter(LithomeNoiseGeneratorSettings::volumeRule)
        ).apply(i, LithomeNoiseGeneratorSettings::new));

        CODEC = RegistryFileCodec.create(LithomeRegistries.NOISE_SETTINGS, DIRECT_CODEC);
    }
}
