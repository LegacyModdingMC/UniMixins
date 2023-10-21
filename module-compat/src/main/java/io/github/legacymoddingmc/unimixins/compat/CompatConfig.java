package io.github.legacymoddingmc.unimixins.compat;

import io.github.legacymoddingmc.unimixins.common.config.AnnotatedProperties;

public class CompatConfig {
    @AnnotatedProperties.ConfigString(def = "true", com = "Remap references to Mixin's shaded ASM.", cat = "compat")
    public static boolean enableRemapper;
    @AnnotatedProperties.ConfigString(def = "true", com = "Include mixin errors and list of applied mixins in crash reports.", cat = "compat")
    public static boolean enhanceCrashReports;
}
