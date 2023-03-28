package io.github.legacymoddingmc.unimixins.common.sanitycheck;

import io.github.legacymoddingmc.unimixins.common.config.AnnotatedProperties;

public class SanityCheckConfig {
    @AnnotatedProperties.ConfigString(def = "true", com = "Throw an error if an invalid combination of modules is detected. Only disable this if you know what you're doing.", cat = "_common")
    public static boolean enableIntegrityChecks;
}
