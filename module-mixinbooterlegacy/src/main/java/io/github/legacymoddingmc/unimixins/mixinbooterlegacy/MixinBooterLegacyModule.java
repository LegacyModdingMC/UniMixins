package io.github.legacymoddingmc.unimixins.mixinbooterlegacy;

import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;

import java.util.Arrays;

public class MixinBooterLegacyModule {
    public static void init() {
        if(SanityCheckHelper.isEnabled()) {
            SanityCheckHelper.warnIfJarPrefixesExist(Arrays.asList("mixinbooterlegacy-"));
        }
    }
}
