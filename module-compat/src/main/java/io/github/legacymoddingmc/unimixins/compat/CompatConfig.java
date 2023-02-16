package io.github.legacymoddingmc.unimixins.compat;

import io.github.legacymoddingmc.unimixins.common.AnnotatedProperties;

public class CompatConfig {
    @AnnotatedProperties.ConfigString(def = "true", com = "Remap references to Mixin's shaded ASM.", cat = "compat")
    public static boolean enableRemapper;
    @AnnotatedProperties.ConfigString(def = "true", com = "Include Mixin errors in crash reports.", cat = "compat")
    public static boolean enhanceCrashReports;
}
