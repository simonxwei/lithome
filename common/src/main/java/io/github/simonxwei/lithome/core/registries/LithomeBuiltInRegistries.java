package io.github.simonxwei.lithome.core.registries;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.levelgen.volume.LithomeVolumeRules;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSources;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @see net.minecraft.core.registries.BuiltInRegistries
 * @author simonxwei
 */
public final class LithomeBuiltInRegistries {
    private static final Map<Identifier, Supplier<?>> LOADERS;
    private static final WritableRegistry<Registry<?>> WRITABLE_REGISTRY;

    public static final Registry<MapCodec<? extends LithomeSource>> LITHOME_SOURCE;
    public static final Registry<MapCodec<? extends LithomeVolumeRules.ConditionSource>> MATERIAL_CONDITION;
    public static final Registry<MapCodec<? extends LithomeVolumeRules.RuleSource>> MATERIAL_RULE;
    public static final Registry<Registry<?>> REGISTRY;

    private LithomeBuiltInRegistries() {}

    // public

    public static void bootstrap() {
        createContents();
        freezeStaticRegistries();
        validate(REGISTRY);
    }

    // core

    private static <T> Registry<T> registerSimple(
            final ResourceKey<Registry<T>> name,
            final RegistryBootstrap<T> loader
    ) {
        return internalRegister(name, new MappedRegistry<>(name, Lifecycle.stable(), false), loader);
    }

    private static void createContents() {
        LOADERS.forEach((key, value) -> {
            if (value.get() == null) {
                Constants.LOGGER.error("Unable to bootstrap registry '{}'", key);
            }
        });
    }

    private static void freezeStaticRegistries() {
        REGISTRY.freeze();
        for (final Registry<?> registry : REGISTRY) {
            bindBootstrappedTagsToEmpty(registry);

            // These two type registries are the public extension points for addon mods.
            // They intentionally remain writable during mod initialization.
            if (registry != MATERIAL_CONDITION && registry != MATERIAL_RULE) {
                registry.freeze();
            }
        }
    }

    private static <T extends Registry<?>> void validate(final Registry<T> rootRegistry) {
        rootRegistry.forEach(registry -> {
            if (registry.keySet().isEmpty()) {
                Util.logAndPauseIfInIde("Registry '" + rootRegistry.getKey(registry) + "' was empty after loading");
            }

            if (registry instanceof DefaultedRegistry<?> defaultedRegistry) {
                final Identifier defaultKey = defaultedRegistry.getDefaultKey();
                Objects.requireNonNull(
                        registry.getValue(defaultKey),
                        "Missing default of DefaultedMappedRegistry: " + defaultKey
                );
            }
        });
    }

    // custom

    private static <T, R extends WritableRegistry<T>> R internalRegister(
            final ResourceKey<Registry<T>> name,
            final R registry,
            final RegistryBootstrap<T> loader
    ) {
        final Identifier key = name.identifier();
        Bootstrap.checkBootstrapCalled(() -> "registry " + key);
        LOADERS.put(key, () -> loader.run(registry));
        WRITABLE_REGISTRY.register(castRootEntryKey(name), registry, RegistrationInfo.BUILT_IN);
        return registry;
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<Registry<?>> castRootEntryKey(final ResourceKey<Registry<T>> key) {
        return (ResourceKey<Registry<?>>) (ResourceKey<?>) key;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void bindBootstrappedTagsToEmpty(final Registry<?> registry) {
        ((MappedRegistry) registry).bindAllTagsToEmpty();
    }

    static {
        LOADERS = Maps.newLinkedHashMap();
        WRITABLE_REGISTRY = new MappedRegistry<>(
                ResourceKey.createRegistryKey(LithomeRegistries.ROOT_REGISTRY_NAME),
                Lifecycle.stable()
        );

        LITHOME_SOURCE = registerSimple(LithomeRegistries.LITHOME_SOURCE, LithomeSources::bootstrap);
        MATERIAL_CONDITION = registerSimple(
                LithomeRegistries.MATERIAL_CONDITION,
                LithomeVolumeRules::bootstrapConditions
        );
        MATERIAL_RULE = registerSimple(
                LithomeRegistries.MATERIAL_RULE,
                LithomeVolumeRules::bootstrapRules
        );

        REGISTRY = WRITABLE_REGISTRY;
    }

    @FunctionalInterface
    private interface RegistryBootstrap<T> {
        T run(Registry<T> registry);
    }
}
