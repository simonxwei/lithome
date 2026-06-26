package io.github.simonxwei.lithome;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

    public static final String MOD_ID = "lithome";
    public static final String MOD_NAME = "Lithome";
    public static final Logger LOGGER;

    private Constants() {}

    // public

    public static Identifier id(final String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    static {
        LOGGER = LoggerFactory.getLogger(MOD_NAME);
    }
}
