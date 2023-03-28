package io.github.legacymoddingmc.unimixins.mixingasm;

import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;

import java.util.Arrays;

public class MixingasmModule {
    public static void init() {
        if(SanityCheckHelper.isEnabled()) {
            SanityCheckHelper.warnIfJarPrefixesExist(Arrays.asList("mixingasm-"));
        }
    }
}
