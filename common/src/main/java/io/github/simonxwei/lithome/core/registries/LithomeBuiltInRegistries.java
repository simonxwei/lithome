package io.github.simonxwei.lithome.core.registries;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.simonxwei.lithome.Constants;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSource;
import io.github.simonxwei.lithome.world.level.lithome.LithomeSources;
import io.github.simonxwei.lithome.world.level.lithome.material.LithomeMaterialSettings;
import io.github.simonxwei.lithome.world.level.lithome.material.LithomeMaterialSettingsTypes;
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

public final class LithomeBuiltInRegistries {
    private static final Map<Identifier, Supplier<?>> LOADERS;
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY;

    public static final Registry<MapCodec<? extends LithomeSource>> LITHOME_SOURCE;
    public static final Registry<MapCodec<? extends LithomeMaterialSettings>> LITHOME_MATERIAL_SETTINGS;
    public static final Registry<WritableRegistry<?>> REGISTRY;

    private LithomeBuiltInRegistries() {
    }

    public static void bootstrap() {
        createContents();
        freeze();
        validate(REGISTRY);
    }

    private static <T> Registry<T> registerSimple(
        final ResourceKey<? extends Registry<T>> name,
        final RegistryBootstrap<T> loader
    ) {
        return internalRegister(
            name,
            new MappedRegistry<>(name, Lifecycle.stable(), false),
            loader
        );
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(
        final ResourceKey<? extends Registry<T>> name,
        final R registry,
        final RegistryBootstrap<T> loader
    ) {
        final Identifier identifier = name.identifier();
        Bootstrap.checkBootstrapCalled(() -> "registry " + identifier);
        LOADERS.put(identifier, () -> loader.run(registry));
        WRITABLE_REGISTRY.register(castRootEntryKey(name), registry, RegistrationInfo.BUILT_IN);
        return registry;
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<WritableRegistry<?>> castRootEntryKey(final ResourceKey<?> key) {
        return (ResourceKey<WritableRegistry<?>>) key;
    }

    private static void createContents() {
        LOADERS.forEach((identifier, loader) -> {
            if (loader.get() == null) {
                Constants.LOGGER.error("Unable to bootstrap registry '{}'", identifier);
            }
        });
    }

    private static void freeze() {
        REGISTRY.freeze();
        for (final Registry<?> registry : REGISTRY) {
            bindBootstrappedTagsToEmpty(registry);
            registry.freeze();
        }
    }

    private static <T extends Registry<?>> void validate(final Registry<T> rootRegistry) {
        rootRegistry.forEach(registry -> {
            if (registry.keySet().isEmpty()) {
                Util.logAndPauseIfInIde(
                    "Registry '" + rootRegistry.getKey(registry) + "' was empty after loading"
                );
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

    private static void bindBootstrappedTagsToEmpty(final Registry<?> registry) {
        ((MappedRegistry<?>) registry).bindAllTagsToEmpty();
    }

    static {
        LOADERS = Maps.newLinkedHashMap();
        WRITABLE_REGISTRY = new MappedRegistry<>(
            ResourceKey.createRegistryKey(LithomeRegistries.ROOT_REGISTRY_NAME),
            Lifecycle.stable()
        );
        LITHOME_MATERIAL_SETTINGS = registerSimple(
            LithomeRegistries.LITHOME_MATERIAL_SETTINGS,
            LithomeMaterialSettingsTypes::bootstrap
        );
        LITHOME_SOURCE = registerSimple(
            LithomeRegistries.LITHOME_SOURCE,
            LithomeSources::bootstrap
        );
        REGISTRY = WRITABLE_REGISTRY;
    }

    @FunctionalInterface
    private interface RegistryBootstrap<T> {
        T run(Registry<T> registry);
    }
}
