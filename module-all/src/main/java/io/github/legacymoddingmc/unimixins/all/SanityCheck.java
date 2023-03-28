package io.github.legacymoddingmc.unimixins.all;

import net.minecraft.launchwrapper.Launch;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.legacymoddingmc.unimixins.all.AllCore.LOGGER;

public class SanityCheck {

    private static List<String> CONCERNING_JAR_PREFIXES = Arrays.asList(
            "gtnhmixins-",
            "gasstation-",
            "mixinbooterlegacy-",
            "spongemixins-",
            "mixingasm-"
    );

    private static final Pattern LETTER = Pattern.compile("[a-z]");

    static void doSanityCheck() {
        List<String> warnings = new ArrayList<>();

        checkConcerningJars(warnings);
        checkMixinContainer(warnings);
        checkMixinHasInitialized(warnings);

        if(!warnings.isEmpty()) {
            // Any throwables we throw here will get caught, so all we can do is warn.
            LOGGER.warn("=======================================================================================");
            LOGGER.warn("WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING");
            LOGGER.warn("=======================================================================================");
            for(String w : warnings) {
                LOGGER.error(" - " + w);
            }
            LOGGER.error("The game will almost certainly crash!");
            LOGGER.fatal("======================================================================================");
            throw new Error(String.join("; ", warnings)); // Attention grabbing stack trace
        }
    }

    private static void checkMixinHasInitialized(List<String> warnings) {
        if(!Launch.blackboard.containsKey("mixin.initialised")) {
            warnings.add("Mixin has not been initialized! If this is a dev environment, make sure org.spongepowered.asm.launch.MixinTweaker was added via the --tweakClass program argument.");
        }
    }

    private static void checkMixinContainer(List<String> warnings) {
        String mixinCoreJarName = getJarName(SanityCheck.class.getResource("/io/github/legacymoddingmc/unimixins/mixin/MixinCore.class"));
        String mixinTweakerJarName = getJarName(SanityCheck.class.getResource("/org/spongepowered/asm/launch/MixinTweaker.class"));

        LOGGER.debug("Name of jar containing MixinCore: " + mixinCoreJarName);
        LOGGER.debug("Name of jar containing MixinTweaker: " + mixinTweakerJarName);

        if(!mixinCoreJarName.equals(mixinTweakerJarName)) {
            boolean isSuspectedToBeDueToNaming =
                    mixinCoreJarName != null && mixinTweakerJarName != null
                        && mixinCoreJarName.compareToIgnoreCase(mixinTweakerJarName) > 0;
            warnings.add("A different version of Mixin (the one inside " + stringOr(mixinTweakerJarName, "unknown") + ") is getting loaded instead of UniMixins's one (inside " + stringOr(mixinCoreJarName, "unknown") + ")!" + (isSuspectedToBeDueToNaming ? " This is probably because because UniMixins's jar name comes later alphabetically. Try renaming it to come first (for example, by adding a '!' character at the beginning of the file name.)" : ""));
        }
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

    private static void checkConcerningJars(List<String> warnings) {
        List<String> concerningJars = getConcerningJars();
        if(!concerningJars.isEmpty()) {
            warnings.add("Detected incompatible jars: " + concerningJars);
        }
    }

    private static List<String> getConcerningJars() {
        List<String> concerningJars = new ArrayList<>();

        for(URL url : Launch.classLoader.getSources()) {
            String path = url.getPath();
            if(path.endsWith(".jar")) {
                String[] components = path.split("/");
                String name = components[components.length - 1].toLowerCase();
                Matcher matcher = LETTER.matcher(name);
                if(matcher.find()) {
                    int firstLetterIndex = matcher.start();
                    name = name.substring(firstLetterIndex);
                    if (anyPrefixesMatch(name, CONCERNING_JAR_PREFIXES)) {
                        concerningJars.add(name);
                    }
                }
            }
        }

        return concerningJars;
    }

    private static boolean anyPrefixesMatch(String s, Collection<String> prefixes) {
        for(String p : prefixes) {
            if(s.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

}
