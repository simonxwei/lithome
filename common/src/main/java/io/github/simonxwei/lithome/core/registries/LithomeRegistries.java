package io.github.simonxwei.lithome.core.registries;

import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.presets.LithomeWorldPreset;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.levelgen.setting.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import io.github.simonxwei.lithome.world.level.lithome.MultiNoiseLithomeSourceParameterList;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * @see net.minecraft.core.registries.Registries
 * @author simonxwei
 */
public final class LithomeRegistries {

    public static final Identifier ROOT_REGISTRY_NAME;
    public static final ResourceKey<Registry<MapCodec<? extends LithomeSource>>> LITHOME_SOURCE;
    public static final ResourceKey<Registry<MapCodec<? extends LithomeVolumeRules.ConditionSource>>> MATERIAL_CONDITION;
    public static final ResourceKey<Registry<MapCodec<? extends LithomeVolumeRules.RuleSource>>> MATERIAL_RULE;
    public static final ResourceKey<Registry<Lithome>> LITHOME;
    public static final ResourceKey<Registry<MultiNoiseLithomeSourceParameterList>> MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST;
    public static final ResourceKey<Registry<LithomeNoiseGeneratorSettings>> NOISE_SETTINGS;
    public static final ResourceKey<Registry<LithomeWorldPreset>> WORLD_PRESET;

    private LithomeRegistries() {}

    // core

    private static <T> ResourceKey<Registry<T>> createRegistryKey(final String name) {
        return ResourceKey.createRegistryKey(Constants.id(name));
    }

    static {
        ROOT_REGISTRY_NAME = Constants.id("root");
        LITHOME_SOURCE = createRegistryKey("worldgen/lithome_source");
        MATERIAL_CONDITION = createRegistryKey("worldgen/material_condition");
        MATERIAL_RULE = createRegistryKey("worldgen/material_rule");
        LITHOME = createRegistryKey("worldgen/lithome");
        MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST = createRegistryKey("worldgen/multi_noise_lithome_source_parameter_list");
        NOISE_SETTINGS = createRegistryKey("worldgen/noise_settings");
        WORLD_PRESET = createRegistryKey("worldgen/world_preset");
    }
}
