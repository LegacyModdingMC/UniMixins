package io.github.legacymoddingmc.unimixins.gasstation;

import io.github.legacymoddingmc.unimixins.common.AnnotatedProperties;

public class GasStationConfig {
    @AnnotatedProperties.ConfigString(def = "false", com = "Don't throw an error if an invalid combination of modules is detected. For advanced users.", cat = "_common")
    public static boolean disableIntegrityChecks;
}
