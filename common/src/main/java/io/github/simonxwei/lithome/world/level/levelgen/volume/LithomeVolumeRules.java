package io.github.simonxwei.lithome.world.level.levelgen.volume;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.core.registries.LithomeBuiltInRegistries;
import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import io.github.simonxwei.lithome.world.level.levelgen.performance.LithomeWorldgenPerformance;
import io.github.simonxwei.lithome.world.level.levelgen.synth.NoiseFractions;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * @see net.minecraft.world.level.levelgen.SurfaceRules
 * @author simonxwei
 */
public final class LithomeVolumeRules {

    private LithomeVolumeRules() {}

    public static RuleSource state(final BlockState state) {
        return new StateRuleSource(state);
    }

    public static RuleSource lithomeDefaultState() {
        return LithomeDefaultStateRuleSource.INSTANCE;
    }

    public static RuleSource sequence(final RuleSource... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("Need at least one Lithome volume rule for a sequence");
        }
        return new SequenceRuleSource(List.of(rules));
    }

    public static RuleSource ifTrue(final ConditionSource condition, final RuleSource rule) {
        return new TestRuleSource(condition, rule);
    }

    public static RuleSource inclusions(
        final ResourceKey<NormalNoise.NoiseParameters> noise,
        final float targetFraction,
        final RuleSource inclusionRule,
        final RuleSource hostRule
    ) {
        return new InclusionsRuleSource(noise, targetFraction, inclusionRule, hostRule);
    }

    public static ConditionSource isLithome(final HolderSet<Lithome> lithomes) {
        return new LithomeConditionSource(lithomes);
    }

    public static ConditionSource noiseThreshold(
        final ResourceKey<NormalNoise.NoiseParameters> noise,
        final double minThreshold,
        final double maxThreshold,
        final boolean is3d
    ) {
        return new NoiseThresholdConditionSource(noise, minThreshold, maxThreshold, is3d);
    }

    public static ConditionSource yRange(
        final VerticalAnchor minInclusive,
        final VerticalAnchor maxInclusive
    ) {
        return new YRangeConditionSource(minInclusive, maxInclusive);
    }

    public static ConditionSource not(final ConditionSource target) {
        return new NotConditionSource(target);
    }

    private static <A> MapCodec<? extends A> register(
        final Registry<MapCodec<? extends A>> registry,
        final String name,
        final MapCodec<? extends A> codec
    ) {
        return Registry.register(registry, Constants.id(name), codec);
    }

    public static MapCodec<? extends RuleSource> bootstrapRules(
        final Registry<MapCodec<? extends RuleSource>> registry
    ) {
        register(registry, "state", StateRuleSource.CODEC);
        register(registry, "lithome_default_state", LithomeDefaultStateRuleSource.CODEC);
        register(registry, "sequence", SequenceRuleSource.CODEC);
        register(registry, "condition", TestRuleSource.CODEC);
        return register(registry, "inclusions", InclusionsRuleSource.CODEC);
    }

    public static MapCodec<? extends ConditionSource> bootstrapConditions(
        final Registry<MapCodec<? extends ConditionSource>> registry
    ) {
        register(registry, "lithome", LithomeConditionSource.CODEC);
        register(registry, "noise_threshold", NoiseThresholdConditionSource.CODEC);
        register(registry, "y_range", YRangeConditionSource.CODEC);
        return register(registry, "not", NotConditionSource.CODEC);
    }

    @FunctionalInterface
    public interface VolumeRule {
        @Nullable BlockState tryApply();
    }

    @FunctionalInterface
    public interface VolumeCondition {
        boolean test();
    }

    public interface RuleSource extends Function<Context, VolumeRule> {
        Codec<RuleSource> CODEC = LithomeBuiltInRegistries.MATERIAL_RULE
            .byNameCodec()
            .dispatch(RuleSource::codec, Function.identity());

        MapCodec<? extends RuleSource> codec();
    }

    public interface ConditionSource extends Function<Context, VolumeCondition> {
        Codec<ConditionSource> CODEC = LithomeBuiltInRegistries.MATERIAL_CONDITION
            .byNameCodec()
            .dispatch(ConditionSource::codec, Function.identity());

        MapCodec<? extends ConditionSource> codec();
    }

    public static final class Context {
        private final RandomState randomState;
        private final LithomeManager lithomeManager;
        private final WorldGenerationContext generationContext;
        private final LithomeWorldgenPerformance.@Nullable VolumeSample performanceSample;
        private final Map<ResourceKey<NormalNoise.NoiseParameters>, DoubleSupplier> noiseSamplers2d = new HashMap<>();
        private final Map<ResourceKey<NormalNoise.NoiseParameters>, DoubleSupplier> noiseSamplers3d = new HashMap<>();

        private int blockX;
        private int blockY;
        private int blockZ;
        private long lastUpdateXZ;
        private long lastUpdateY;
        private long cachedLithomeUpdateY = Long.MIN_VALUE;
        private @Nullable Holder<Lithome> cachedLithome;

        public Context(
                final RandomState randomState,
                final LithomeManager lithomeManager,
                final WorldGenerationContext generationContext
        ) {
            this(randomState, lithomeManager, generationContext, null);
        }

        public Context(
                final RandomState randomState,
                final LithomeManager lithomeManager,
                final WorldGenerationContext generationContext,
                final LithomeWorldgenPerformance.@Nullable VolumeSample performanceSample
        ) {
            this.randomState = randomState;
            this.lithomeManager = lithomeManager;
            this.generationContext = generationContext;
            this.performanceSample = performanceSample;
        }

        public void updateXZ(final int blockX, final int blockZ) {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = blockX;
            this.blockZ = blockZ;
            this.cachedLithome = null;
        }

        public void updateY(final int blockY) {
            ++this.lastUpdateY;
            this.blockY = blockY;
            this.cachedLithome = null;
        }

        public Holder<Lithome> currentLithome() {
            if (this.cachedLithome == null || this.cachedLithomeUpdateY != this.lastUpdateY) {
                if (this.performanceSample != null) {
                    this.performanceSample.incrementLithomeQueries();
                }

                this.cachedLithome = this.lithomeManager.getLithome(this.blockX, this.blockY, this.blockZ);
                this.cachedLithomeUpdateY = this.lastUpdateY;
            }

            return this.cachedLithome;
        }

        public int blockY() {
            return this.blockY;
        }

        public WorldGenerationContext generationContext() {
            return this.generationContext;
        }

        public DoubleSupplier noiseSampler(
            final ResourceKey<NormalNoise.NoiseParameters> noiseKey,
            final boolean is3d
        ) {
            return is3d
                ? this.noiseSamplers3d.computeIfAbsent(noiseKey, this::createNoiseSampler3d)
                : this.noiseSamplers2d.computeIfAbsent(noiseKey, this::createNoiseSampler2d);
        }

        private DoubleSupplier createNoiseSampler2d(
            final ResourceKey<NormalNoise.NoiseParameters> noiseKey
        ) {
            final NormalNoise noise = this.randomState.getOrCreateNoise(noiseKey);
            return new DoubleSupplier() {
                private long sampledAtXZ = Context.this.lastUpdateXZ - 1L;
                private double value;

                @Override
                public double getAsDouble() {
                    if (this.sampledAtXZ != Context.this.lastUpdateXZ) {
                        if (Context.this.performanceSample != null) {
                            Context.this.performanceSample.incrementNoiseSamples2d();
                        }

                        this.value = noise.getValue(Context.this.blockX, 0.0D, Context.this.blockZ);
                        this.sampledAtXZ = Context.this.lastUpdateXZ;
                    }
                    return this.value;
                }
            };
        }

        private DoubleSupplier createNoiseSampler3d(
            final ResourceKey<NormalNoise.NoiseParameters> noiseKey
        ) {
            final NormalNoise noise = this.randomState.getOrCreateNoise(noiseKey);
            return new DoubleSupplier() {
                private long sampledAtY = Context.this.lastUpdateY - 1L;
                private double value;

                @Override
                public double getAsDouble() {
                    if (this.sampledAtY != Context.this.lastUpdateY) {
                        if (Context.this.performanceSample != null) {
                            Context.this.performanceSample.incrementNoiseSamples3d();
                        }

                        this.value = noise.getValue(
                                Context.this.blockX,
                                Context.this.blockY,
                                Context.this.blockZ
                        );
                        this.sampledAtY = Context.this.lastUpdateY;
                    }
                    return this.value;
                }
            };
        }
    }

    private record StateRuleSource(BlockState state, VolumeRule rule) implements RuleSource {
        private static final MapCodec<StateRuleSource> CODEC = BlockState.CODEC
            .xmap(StateRuleSource::new, StateRuleSource::state)
            .fieldOf("state");

        private StateRuleSource(final BlockState state) {
            this(state, () -> state);
        }

        @Override
        public MapCodec<StateRuleSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeRule apply(final Context context) {
            return this.rule;
        }
    }

    private enum LithomeDefaultStateRuleSource implements RuleSource {
        INSTANCE;

        private static final MapCodec<LithomeDefaultStateRuleSource> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<LithomeDefaultStateRuleSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeRule apply(final Context context) {
            return () -> context.currentLithome().value().getDefaultBlock();
        }
    }

    private record TestRuleSource(
        ConditionSource ifTrue,
        RuleSource thenRun
    ) implements RuleSource {
        private static final MapCodec<TestRuleSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ConditionSource.CODEC.fieldOf("if_true").forGetter(TestRuleSource::ifTrue),
            RuleSource.CODEC.fieldOf("then_run").forGetter(TestRuleSource::thenRun)
        ).apply(instance, TestRuleSource::new));

        @Override
        public MapCodec<TestRuleSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeRule apply(final Context context) {
            final VolumeCondition condition = this.ifTrue.apply(context);
            final VolumeRule followup = this.thenRun.apply(context);
            return () -> condition.test() ? followup.tryApply() : null;
        }
    }

    private record SequenceRuleSource(List<RuleSource> sequence) implements RuleSource {
        private static final Codec<List<RuleSource>> NON_EMPTY_SEQUENCE_CODEC = RuleSource.CODEC
            .listOf()
            .validate(sequence -> sequence.isEmpty()
                ? DataResult.error(() -> "A Lithome volume-rule sequence must contain at least one rule")
                : DataResult.success(sequence));

        private static final MapCodec<SequenceRuleSource> CODEC = NON_EMPTY_SEQUENCE_CODEC
            .xmap(SequenceRuleSource::new, SequenceRuleSource::sequence)
            .fieldOf("sequence");

        private SequenceRuleSource {
            sequence = List.copyOf(sequence);
        }

        @Override
        public MapCodec<SequenceRuleSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeRule apply(final Context context) {
            if (this.sequence.size() == 1) {
                return this.sequence.getFirst().apply(context);
            }

            final ImmutableList.Builder<VolumeRule> builder = ImmutableList.builder();
            for (final RuleSource rule : this.sequence) {
                builder.add(rule.apply(context));
            }
            final List<VolumeRule> rules = builder.build();

            return () -> {
                for (final VolumeRule rule : rules) {
                    final BlockState result = rule.tryApply();
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            };
        }
    }

    private record InclusionsRuleSource(
        ResourceKey<NormalNoise.NoiseParameters> noise,
        float targetFraction,
        RuleSource inclusionRule,
        RuleSource hostRule
    ) implements RuleSource {
        private static final MapCodec<InclusionsRuleSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.NOISE)
                .fieldOf("noise")
                .forGetter(InclusionsRuleSource::noise),
            Codec.floatRange(0.0F, 1.0F)
                .fieldOf("target_fraction")
                .forGetter(InclusionsRuleSource::targetFraction),
            RuleSource.CODEC
                .fieldOf("inclusion_rule")
                .forGetter(InclusionsRuleSource::inclusionRule),
            RuleSource.CODEC
                .fieldOf("host_rule")
                .forGetter(InclusionsRuleSource::hostRule)
        ).apply(instance, InclusionsRuleSource::new));

        @Override
        public MapCodec<InclusionsRuleSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeRule apply(final Context context) {
            final DoubleSupplier noiseSampler = context.noiseSampler(this.noise, true);
            final double threshold = NoiseFractions.upperTailThreshold(this.targetFraction);
            final VolumeRule inclusion = this.inclusionRule.apply(context);
            final VolumeRule host = this.hostRule.apply(context);

            return () -> (noiseSampler.getAsDouble() >= threshold ? inclusion : host).tryApply();
        }
    }

    private record LithomeConditionSource(
        HolderSet<Lithome> lithomes
    ) implements ConditionSource {
        private static final MapCodec<LithomeConditionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(LithomeRegistries.LITHOME)
                .fieldOf("lithome_is")
                .forGetter(LithomeConditionSource::lithomes)
        ).apply(instance, LithomeConditionSource::new));

        @Override
        public MapCodec<LithomeConditionSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeCondition apply(final Context context) {
            return () -> this.lithomes.contains(context.currentLithome());
        }
    }

    private record NoiseThresholdConditionSource(
        ResourceKey<NormalNoise.NoiseParameters> noise,
        double minThreshold,
        double maxThreshold,
        boolean is3d
    ) implements ConditionSource {
        private static final MapCodec<NoiseThresholdConditionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.NOISE)
                .fieldOf("noise")
                .forGetter(NoiseThresholdConditionSource::noise),
            Codec.DOUBLE
                .fieldOf("min_threshold")
                .forGetter(NoiseThresholdConditionSource::minThreshold),
            Codec.DOUBLE
                .fieldOf("max_threshold")
                .forGetter(NoiseThresholdConditionSource::maxThreshold),
            Codec.BOOL
                .optionalFieldOf("is_3d", true)
                .forGetter(NoiseThresholdConditionSource::is3d)
        ).apply(instance, NoiseThresholdConditionSource::new));

        private NoiseThresholdConditionSource {
            if (minThreshold > maxThreshold) {
                throw new IllegalArgumentException("min_threshold must not exceed max_threshold");
            }
        }

        @Override
        public MapCodec<NoiseThresholdConditionSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeCondition apply(final Context context) {
            final DoubleSupplier noiseSampler = context.noiseSampler(this.noise, this.is3d);
            return () -> {
                final double value = noiseSampler.getAsDouble();
                return value >= this.minThreshold && value <= this.maxThreshold;
            };
        }
    }

    private record YRangeConditionSource(
        VerticalAnchor minInclusive,
        VerticalAnchor maxInclusive
    ) implements ConditionSource {
        private static final MapCodec<YRangeConditionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            VerticalAnchor.CODEC
                .fieldOf("min_inclusive")
                .forGetter(YRangeConditionSource::minInclusive),
            VerticalAnchor.CODEC
                .fieldOf("max_inclusive")
                .forGetter(YRangeConditionSource::maxInclusive)
        ).apply(instance, YRangeConditionSource::new));

        @Override
        public MapCodec<YRangeConditionSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeCondition apply(final Context context) {
            final int minimumY = this.minInclusive.resolveY(context.generationContext());
            final int maximumY = this.maxInclusive.resolveY(context.generationContext());
            if (minimumY > maximumY) {
                throw new IllegalStateException("Resolved min_inclusive is above max_inclusive");
            }
            return () -> context.blockY() >= minimumY && context.blockY() <= maximumY;
        }
    }

    private record NotConditionSource(ConditionSource target) implements ConditionSource {
        private static final MapCodec<NotConditionSource> CODEC = ConditionSource.CODEC
            .xmap(NotConditionSource::new, NotConditionSource::target)
            .fieldOf("invert");

        @Override
        public MapCodec<NotConditionSource> codec() {
            return CODEC;
        }

        @Override
        public VolumeCondition apply(final Context context) {
            final VolumeCondition targetCondition = this.target.apply(context);
            return () -> !targetCondition.test();
        }
    }
}
