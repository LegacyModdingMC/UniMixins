package io.github.legacymoddingmc.unimixins.common.sanitycheck;

import io.github.legacymoddingmc.unimixins.common.config.ConfigUtil;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanityCheckHelper {

    private static final Logger LOGGER = LogManager.getLogger("unimixins");

    private static final Pattern LETTER = Pattern.compile("[a-z]");

    static {
        ConfigUtil.load(SanityCheckConfig.class);
    }

    public static boolean isEnabled() {
        if(!SanityCheckConfig.enableIntegrityChecks && !Launch.blackboard.containsKey("unimixins.warnedAboutDisabledIntegrityChecks")) {
            LOGGER.debug("Skipping sanity checks because integrity checks are disabled in the config.");
            // only warn once
            Launch.blackboard.put("unimixins.warnedAboutDisabledIntegrityChecks", "true");
        }
        return SanityCheckConfig.enableIntegrityChecks;
    }

    public static void showBigWarning(String warning) {
        showBigWarning(Arrays.asList(warning));
    }

    public static void showBigWarning(List<String>... warningses) {
        List<String> allWarnings = new ArrayList<>();
        for(List<String> l : warningses) {
            allWarnings.addAll(l);
        }

        if(!allWarnings.isEmpty()) {
            // Any throwables we throw here will get caught, so all we can do is warn.
            LOGGER.warn("=======================================================================================");
            LOGGER.warn("WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING");
            LOGGER.warn("=======================================================================================");
            for(String w : allWarnings) {
                LOGGER.error(" - " + w);
            }
            LOGGER.error("The game will almost certainly crash!");
            LOGGER.fatal("======================================================================================");
            throw new Error(String.join("; ", allWarnings)); // Attention grabbing stack trace
        }
    }

    public static void warnIfJarPrefixesExist(List<String> concerningPrefixes) {
        showBigWarning(checkIfJarPrefixesExist(concerningPrefixes));
    }

    public static List<String> checkIfJarPrefixesExist(List<String> concerningPrefixes) {
        List<String> concerningJars = getJarsMatchingPrefix(concerningPrefixes);
        if(!concerningJars.isEmpty()) {
            return Arrays.asList("Detected incompatible jars: " + concerningJars);
        }
        return Arrays.asList();
    }

    private static List<String> getJarsMatchingPrefix(List<String> concerningPrefixes) {
        List<String> matches = new ArrayList<>();

        for(URL url : Launch.classLoader.getSources()) {
            String path = url.getPath();
            if(path.endsWith(".jar")) {
                String[] components = path.split("/");
                String name = components[components.length - 1].toLowerCase();
                Matcher matcher = LETTER.matcher(name);
                if(matcher.find()) {
                    int firstLetterIndex = matcher.start();
                    if (anyPrefixesMatch(name.substring(firstLetterIndex), concerningPrefixes)) {
                        matches.add(name);
                    }
                }
            }
        }

        return matches;
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
