package io.github.simonxwei.lithome.world.level.levelgen.presets;

import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.lithome.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Map;

public final class LithomeWorldPresets {

    public static final ResourceKey<LithomeWorldPreset> NORMAL;

    private LithomeWorldPresets() {}

    // public

    public static void bootstrap(final BootstrapContext<LithomeWorldPreset> context) {
        final Bootstrap bootstrap = new Bootstrap(context);
        bootstrap.bootstrap();
    }

    // core

    private static ResourceKey<LithomeWorldPreset> register(final String name) {
        return ResourceKey.create(LithomeRegistries.WORLD_PRESET, Constants.id(name));
    }

    static {
        NORMAL = register("normal");
    }

    private static final class Bootstrap {

        private final BootstrapContext<LithomeWorldPreset> context;
        private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
        private final HolderGetter<MultiNoiseLithomeSourceParameterList> multiNoiseBiomeSourceParameterLists;
        private final Holder<DimensionType> overworldDimensionType;

        private Bootstrap(final BootstrapContext<LithomeWorldPreset> context) {
            this.context = context;
            final HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
            this.noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
            this.multiNoiseBiomeSourceParameterLists = context.lookup(LithomeRegistries.MULTI_NOISE_LITHOME_SOURCE_PARAMETER_LIST);
            this.overworldDimensionType = dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        }

        // public

        public void bootstrap() {
            final Holder.Reference<MultiNoiseLithomeSourceParameterList> overworldPreset = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseLithomeSourceParameterLists.OVERWORLD);
            this.registerOverworlds(MultiNoiseLithomeSource.createFromPreset(overworldPreset));
        }

        // core

        private void registerOverworlds(final LithomeSource lithomeSource) {
            final Holder<NoiseGeneratorSettings> overworldNoiseSettings = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.registerCustomOverworldPreset(LithomeWorldPresets.NORMAL, this.makeNoiseBasedOverworld(lithomeSource, overworldNoiseSettings));
        }

        // custom

        private void registerCustomOverworldPreset(final ResourceKey<LithomeWorldPreset> debug, final LevelStem overworld) {
            this.context.register(debug, this.createPresetWithCustomOverworld(overworld));
        }

        private LithomeWorldPreset createPresetWithCustomOverworld(final LevelStem overworldStem) {
            return new LithomeWorldPreset(Map.of(LevelStem.OVERWORLD, overworldStem));
        }

        private LevelStem makeNoiseBasedOverworld(final LithomeSource overworldLithomeSource, final Holder<NoiseGeneratorSettings> noiseSettings) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(overworldLithomeSource, noiseSettings));
        }

        private LevelStem makeOverworld(final ChunkGenerator generator) {
            return new LevelStem(this.overworldDimensionType, generator);
        }
    }
}
