package io.github.legacymoddingmc.unimixins.all;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.common.ConfigUtil;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class AllCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    private static List<Class<?>> embeddedCorePluginClasses = new ArrayList<>();
    private static List<IFMLLoadingPlugin> embeddedCorePluginInstances = new ArrayList<>();

    private static List<String> CONCERNING_JAR_PREFIXES = Arrays.asList(
            "gtnhmixins-",
            "gasstation-",
            "mixinbooterlegacy-",
            "spongemixins-",
            "mixingasm-"
    );

    private static final Pattern LETTER = Pattern.compile("[a-z]");

    static {
        ConfigUtil.load(AllConfig.class);
        if(!AllConfig.disableIntegrityChecks) {
            doSanityCheck();
        }

        try {
            for(String s : IOUtils.toString(AllCore.class.getResource("/META-INF/unimixins-all.EmbeddedFMLCorePlugins.txt")).split(" ")) {
                Class<?> cls = Class.forName(s);
                embeddedCorePluginClasses.add(cls);
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void doSanityCheck() {
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

        if(!concerningJars.isEmpty()) {
            // Any throwables we throw here will get caught, so all we can do is warn.
            String theWarning = "Detected incompatible jars: " + concerningJars;

            LOGGER.warn("=======================================================================================");
            LOGGER.warn("WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING / WARNING");
            LOGGER.warn("=======================================================================================");
            LOGGER.error(theWarning);
            LOGGER.error("The game will almost certainly crash!");
            LOGGER.fatal("======================================================================================");
            throw new Error(theWarning); // Attention grabbing stack trace
        }
    }

    private static boolean anyPrefixesMatch(String s, Collection<String> prefixes) {
        for(String p : prefixes) {
            if(s.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    public AllCore() {
        LOGGER.info("Instantiating AllCore");

        for(Class<?> cls : embeddedCorePluginClasses) {
            try {
                embeddedCorePluginInstances.add((IFMLLoadingPlugin)cls.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        List<String> classes = new ArrayList<>();
        for(IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            for(String s : p.getASMTransformerClass()) {
                classes.add(s);
            }
        }
        return classes.toArray(new String[0]);
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        for(IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            p.injectData(data);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}
