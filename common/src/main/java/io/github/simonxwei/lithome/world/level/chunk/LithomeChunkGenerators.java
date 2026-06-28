package io.github.simonxwei.lithome.world.level.chunk;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseBasedChunkGeneratorExtension;
import io.github.simonxwei.lithome.world.level.levelgen.LithomeNoiseGeneratorSettings;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * @see net.minecraft.world.level.chunk.ChunkGenerators
 * @author simonxwei
 */
public final class LithomeChunkGenerators {

    public static final MapCodec<NoiseBasedChunkGenerator> CODEC;

    private LithomeChunkGenerators() {}

    // public

    /**
     * Fabric 端使用原版 Registry API 注册；NeoForge 端由 DeferredRegister
     * 直接注册 {@link #CODEC}，因此不会调用本方法。
     */
    public static void init() {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            Constants.id("noise"),
            CODEC
        );
    }

    public static NoiseBasedChunkGenerator create(
        final BiomeSource biomeSource,
        final LithomeSource lithomeSource,
        final Holder<NoiseGeneratorSettings> noiseSettings,
        final Holder<LithomeNoiseGeneratorSettings> lithomeNoiseSettings
    ) {
        /*
         * 不得在 Codec 解码阶段调用 lithomeNoiseSettings.value()。
         * world_preset 可能先于 Lithome 自定义 noise_settings 注册表完成加载，
         * 此时 Holder 已经创建但尚未绑定；原版 NoiseGeneratorSettings Holder
         * 则可以直接交给原版生成器构造器，并由生成器在运行期按需解引用。
         */
        final NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
            biomeSource,
            noiseSettings
        );

        extension(generator).lithome$configure(lithomeSource, lithomeNoiseSettings);
        return generator;
    }

    // core

    private static LithomeNoiseBasedChunkGeneratorExtension extension(
        final NoiseBasedChunkGenerator generator
    ) {
        return (LithomeNoiseBasedChunkGeneratorExtension) (Object) generator;
    }

    static {
        CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC
                .fieldOf("biome_source")
                .forGetter(NoiseBasedChunkGenerator::getBiomeSource),
            LithomeSource.CODEC
                .fieldOf("lithome_source")
                .forGetter(generator -> extension(generator).lithome$getConfiguredLithomeSource()),
            NoiseGeneratorSettings.CODEC
                .fieldOf("settings")
                .forGetter(NoiseBasedChunkGenerator::generatorSettings),
            LithomeNoiseGeneratorSettings.CODEC
                .fieldOf("lithome_settings")
                .forGetter(generator -> extension(generator).lithome$getConfiguredNoiseSettings())
        ).apply(i, i.stable(LithomeChunkGenerators::create)));
    }
}
