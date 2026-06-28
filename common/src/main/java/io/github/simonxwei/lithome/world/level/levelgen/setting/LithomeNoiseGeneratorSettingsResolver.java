package io.github.simonxwei.lithome.world.level.levelgen.setting;

import io.github.simonxwei.lithome.core.registries.LithomeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.Optional;

/**
 * Vanilla source references:
 * - net.minecraft.core.RegistryAccess
 * - net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
 *
 * There is no direct vanilla counterpart. This resolver enforces that a
 * NoiseGeneratorSettings holder matches at most one world-level Lithome setup.
 */
public final class LithomeNoiseGeneratorSettingsResolver {
    private LithomeNoiseGeneratorSettingsResolver() {
    }

    public static Optional<LithomeNoiseGeneratorSettings> resolve(
        final RegistryAccess registryAccess,
        final Holder<NoiseGeneratorSettings> noiseSettings
    ) {
        final Registry<LithomeNoiseGeneratorSettings> registry = registryAccess.lookupOrThrow(
            LithomeRegistries.NOISE_SETTINGS
        );

        final List<Holder.Reference<LithomeNoiseGeneratorSettings>> matches = registry
            .listElements()
            .filter(holder -> holder.value().noiseSettings().contains(noiseSettings))
            .toList();

        if (matches.isEmpty()) {
            return Optional.empty();
        }
        if (matches.size() > 1) {
            final String matchingKeys = matches.stream()
                .map(holder -> holder.key().identifier().toString())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("<unknown>");
            throw new IllegalStateException(
                "NoiseGeneratorSettings " + noiseSettings.unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("<direct>")
                    + " matches multiple LithomeGeneratorSettings: " + matchingKeys
            );
        }

        return Optional.of(matches.getFirst().value());
    }
}
