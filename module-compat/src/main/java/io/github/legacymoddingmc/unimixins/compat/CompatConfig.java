package io.github.legacymoddingmc.unimixins.compat;

import io.github.legacymoddingmc.unimixins.common.config.AnnotatedProperties;

public class CompatConfig {
    @AnnotatedProperties.ConfigString(def = "true", com = "Remap references to Mixin's shaded ASM.", cat = "compat")
    public static boolean enableRemapper;
    @AnnotatedProperties.ConfigString(def = "true", com = "Include mixin errors and list of applied mixins in crash reports.", cat = "compat")
    public static boolean enhanceCrashReports;
    @AnnotatedProperties.ConfigString(def = "true", com = "Provide the deprecated `com.gtnewhorizons.mixinextras` relocation of MixinExtras 0.1.1. May be required by some older mods.", cat = "compat")
    public static boolean enableLegacyGTNHMixinExtrasPackage;
    @AnnotatedProperties.ConfigString(def = "false", com = "Fix Forge mods not being discovered if they are inside classpath mods using the Mixin tweaker. Only needed in dev environments, and not needed if using UniMix or GTNH's Mixin fork.", cat = "compat.dev", devOnly = true)
    public static boolean fixClasspathModDiscovery;
}
