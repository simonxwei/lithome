package io.github.simonxwei.lithome.world.level.levelgen.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;
import java.util.Optional;

/**
 * @see net.minecraft.world.level.levelgen.presets.WorldPreset
 * @author simonxwei
 */
public final class LithomeWorldPreset {

    public static final Codec<LithomeWorldPreset> DIRECT_CODEC;
    public static final Codec<Holder<LithomeWorldPreset>> CODEC;

    private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

    public LithomeWorldPreset(final Map<ResourceKey<LevelStem>, LevelStem> dimensions) {
        this.dimensions = dimensions;
    }

    // public

    public Map<ResourceKey<LevelStem>, LevelStem> dimensions() {
        return this.dimensions;
    }

    public Optional<LevelStem> overworld() {
        return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
    }

    // core

    private static DataResult<LithomeWorldPreset> requireOverworld(final LithomeWorldPreset preset) {
        return preset.overworld().isEmpty() ? DataResult.error(() -> "Missing overworld dimension") : DataResult.success(preset, Lifecycle.stable());
    }

    static {
        DIRECT_CODEC = RecordCodecBuilder.<LithomeWorldPreset>create(i -> i.group(
                Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter(LithomeWorldPreset::dimensions)
        ).apply(i, LithomeWorldPreset::new)).validate(LithomeWorldPreset::requireOverworld);
        CODEC = RegistryFileCodec.create(LithomeRegistries.WORLD_PRESET, DIRECT_CODEC);
    }
}
