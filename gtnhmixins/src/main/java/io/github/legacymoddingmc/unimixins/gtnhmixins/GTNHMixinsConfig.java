package io.github.legacymoddingmc.unimixins.gtnhmixins;

import io.github.legacymoddingmc.unimixins.common.config.AnnotatedProperties;

public class GTNHMixinsConfig {
    @AnnotatedProperties.ConfigString(def = "true", com = "Provide the deprecated `com.gtnewhorizons.mixinextras` relocation of MixinExtras 0.1.1. May be required by some older mods.", cat = "gtnhmixins")
    public static boolean enableLegacyGTNHMixinExtrasPackage;
}
