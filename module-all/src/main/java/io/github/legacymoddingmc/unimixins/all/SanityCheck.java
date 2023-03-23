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

        List<String> concerningJars = getConcerningJars();
        if(!concerningJars.isEmpty()) {
            warnings.add("Detected incompatible jars: " + concerningJars);
        }

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
