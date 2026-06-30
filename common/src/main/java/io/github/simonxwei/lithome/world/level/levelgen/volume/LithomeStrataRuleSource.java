package io.github.simonxwei.lithome.world.level.levelgen.volume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleSupplier;

/**
 * 无直接对应的 Minecraft 原版类。
 *
 * 相关参考类：
 * - net.minecraft.world.level.levelgen.SurfaceRules
 * - net.minecraft.world.level.levelgen.synth.NormalNoise
 *
 * 周期层状体积规则。undulation 负责大尺度二维整体起伏，detail_undulation
 * 负责小尺度二维边界细节，disturbance 负责随高度变化的三维局部扰动。
 * 每个层条目持有完整的 Lithome RuleSource，因此可以继续嵌套 inclusions、
 * condition 或命名 volume_rule 引用。
 *
 * @author simonxwei
 */
public record LithomeStrataRuleSource(
    VerticalAnchor baseY,
    Optional<NoiseOffset> undulation,
    Optional<NoiseOffset> detailUndulation,
    Optional<NoiseOffset> disturbance,
    List<Layer> layers
) implements LithomeVolumeRules.RuleSource {

    private static final Codec<List<Layer>> LAYERS_CODEC = Layer.CODEC
        .listOf()
        .validate(LithomeStrataRuleSource::validateLayers);

    public static final MapCodec<LithomeStrataRuleSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        VerticalAnchor.CODEC
            .fieldOf("base_y")
            .forGetter(LithomeStrataRuleSource::baseY),
        NoiseOffset.CODEC
            .optionalFieldOf("undulation")
            .forGetter(LithomeStrataRuleSource::undulation),
        NoiseOffset.CODEC
            .optionalFieldOf("detail_undulation")
            .forGetter(LithomeStrataRuleSource::detailUndulation),
        NoiseOffset.CODEC
            .optionalFieldOf("disturbance")
            .forGetter(LithomeStrataRuleSource::disturbance),
        LAYERS_CODEC
            .fieldOf("layers")
            .forGetter(LithomeStrataRuleSource::layers)
    ).apply(instance, LithomeStrataRuleSource::new));

    public LithomeStrataRuleSource {
        Objects.requireNonNull(baseY, "baseY");
        Objects.requireNonNull(undulation, "undulation");
        Objects.requireNonNull(detailUndulation, "detailUndulation");
        Objects.requireNonNull(disturbance, "disturbance");
        Objects.requireNonNull(layers, "layers");

        layers = List.copyOf(layers);
        final DataResult<List<Layer>> validation = validateLayers(layers);
        validation.error().ifPresent(error -> {
            throw new IllegalArgumentException(error.message());
        });
    }

    @Override
    public MapCodec<LithomeStrataRuleSource> codec() {
        return CODEC;
    }

    @Override
    public LithomeVolumeRules.VolumeRule apply(final LithomeVolumeRules.Context context) {
        final int resolvedBaseY = this.baseY.resolveY(context.generationContext());

        final NoiseOffset undulationConfig = this.undulation.orElse(null);
        final NoiseOffset detailUndulationConfig = this.detailUndulation.orElse(null);
        final NoiseOffset disturbanceConfig = this.disturbance.orElse(null);

        final DoubleSupplier undulationSampler = undulationConfig == null || undulationConfig.amplitude() == 0.0D
            ? null
            : context.noiseSampler2d(undulationConfig.noise());
        final DoubleSupplier detailUndulationSampler = detailUndulationConfig == null
            || detailUndulationConfig.amplitude() == 0.0D
            ? null
            : context.noiseSampler2d(detailUndulationConfig.noise());
        final DoubleSupplier disturbanceSampler = disturbanceConfig == null || disturbanceConfig.amplitude() == 0.0D
            ? null
            : context.noiseSampler3d(disturbanceConfig.noise());

        final int layerCount = this.layers.size();
        final int[] cumulativeThickness = new int[layerCount];
        final LithomeVolumeRules.VolumeRule[] boundRules = new LithomeVolumeRules.VolumeRule[layerCount];

        int totalThickness = 0;
        for (int index = 0; index < layerCount; ++index) {
            final Layer layer = this.layers.get(index);
            totalThickness = Math.addExact(totalThickness, layer.thickness());
            cumulativeThickness[index] = totalThickness;
            boundRules[index] = layer.rule().apply(context);
        }

        final int period = totalThickness;
        return () -> {
            double localLayerPosition = context.blockY() - resolvedBaseY;

            if (undulationSampler != null) {
                localLayerPosition += undulationSampler.getAsDouble() * undulationConfig.amplitude();
            }
            if (detailUndulationSampler != null) {
                localLayerPosition += detailUndulationSampler.getAsDouble() * detailUndulationConfig.amplitude();
            }
            if (disturbanceSampler != null) {
                localLayerPosition += disturbanceSampler.getAsDouble() * disturbanceConfig.amplitude();
            }

            final long flooredPosition = (long) Math.floor(localLayerPosition);
            final int positionInPeriod = (int) Math.floorMod(flooredPosition, (long) period);
            final int searchedThickness = positionInPeriod + 1;

            int layerIndex = Arrays.binarySearch(cumulativeThickness, searchedThickness);
            if (layerIndex < 0) {
                layerIndex = -layerIndex - 1;
            }

            return boundRules[layerIndex].tryApply();
        };
    }

    private static DataResult<List<Layer>> validateLayers(final List<Layer> layers) {
        if (layers.isEmpty()) {
            return DataResult.error(() -> "A strata rule must contain at least one layer");
        }

        long totalThickness = 0L;
        for (final Layer layer : layers) {
            totalThickness += layer.thickness();
            if (totalThickness > Integer.MAX_VALUE) {
                return DataResult.error(() -> "The total strata thickness exceeds the supported integer range");
            }
        }

        return DataResult.success(List.copyOf(layers));
    }

    public record NoiseOffset(
        ResourceKey<NormalNoise.NoiseParameters> noise,
        double amplitude
    ) {
        private static final Codec<NoiseOffset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.NOISE)
                .fieldOf("noise")
                .forGetter(NoiseOffset::noise),
            Codec.doubleRange(0.0D, 1024.0D)
                .fieldOf("amplitude")
                .forGetter(NoiseOffset::amplitude)
        ).apply(instance, NoiseOffset::new));

        public NoiseOffset {
            Objects.requireNonNull(noise, "noise");
        }
    }

    public record Layer(
        int thickness,
        LithomeVolumeRules.RuleSource rule
    ) {
        private static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1_000_000)
                .fieldOf("thickness")
                .forGetter(Layer::thickness),
            LithomeVolumeRules.RuleSource.CODEC
                .fieldOf("rule")
                .forGetter(Layer::rule)
        ).apply(instance, Layer::new));

        public Layer {
            Objects.requireNonNull(rule, "rule");
        }
    }
}
