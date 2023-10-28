package io.github.legacymoddingmc.unimixins.mixin;

import net.minecraft.launchwrapper.Launch;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static io.github.legacymoddingmc.unimixins.mixin.MixinCore.LOGGER;

public class MixinSanityCheck {

    static List<String> checkMixinHasInitialized() {
        if(!Launch.blackboard.containsKey("mixin.initialised")) {
            return Arrays.asList("Mixin has not been initialized! If this is a dev environment, make sure org.spongepowered.asm.launch.MixinTweaker was added via the --tweakClass program argument.");
        }
        return Arrays.asList();
    }

    static List<String> checkMixinContainer() {
        String mixinCoreJarName = getJarName(MixinSanityCheck.class.getResource("/io/github/legacymoddingmc/unimixins/mixin/MixinCore.class"));
        String mixinTweakerJarName = getJarName(MixinSanityCheck.class.getResource("/org/spongepowered/asm/launch/MixinTweaker.class"));

        LOGGER.debug("Name of jar containing MixinCore: " + mixinCoreJarName);
        LOGGER.debug("Name of jar containing MixinTweaker: " + mixinTweakerJarName);

        if(!mixinCoreJarName.equals(mixinTweakerJarName)) {
            boolean isSuspectedToBeDueToNaming =
                    mixinCoreJarName != null && mixinTweakerJarName != null
                            && mixinCoreJarName.compareToIgnoreCase(mixinTweakerJarName) > 0;
            return Arrays.asList("A different version of Mixin (the one inside " + stringOr(mixinTweakerJarName, "unknown") + ") is getting loaded instead of UniMixins's one (inside " + stringOr(mixinCoreJarName, "unknown") + ")!" + (isSuspectedToBeDueToNaming ? " This is probably because because UniMixins's jar name comes later alphabetically. Try renaming it to come first (for example, by adding a '!' character at the beginning of the file name)." : ""));
        }
        return Arrays.asList();
    }

    private static String stringOr(String s, String alternative) {
        return s == null ? alternative : s;
    }

    private static String getJarName(URL url) {
        if(url != null) {
            String urlStr = url.toString();
            if (urlStr.contains("!/")) {
                int exclamationIdx = urlStr.indexOf("!/");
                String preExclamationSubstring = urlStr.substring(0, exclamationIdx);
                int lastSlash = preExclamationSubstring.lastIndexOf('/');
                if (lastSlash != -1) {
                    return preExclamationSubstring.substring(lastSlash + 1);
                }
            }
        }
        return null;
    }
}
