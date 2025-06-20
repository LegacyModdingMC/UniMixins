package io.github.legacymoddingmc.unimixins.all;

import java.net.URL;
import java.util.*;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class AllCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    private static final List<Class<?>> embeddedCorePluginClasses = new ArrayList<>();
    private static final List<IFMLLoadingPlugin> embeddedCorePluginInstances = new ArrayList<>();
    private static final URL embeddedCorePluginsFile = AllCore.class.getResource("/META-INF/EmbeddedFMLCorePlugins.txt");

    static {
        try {
            // The file contains a duplicate, and I can't be bothered to remove it at build time.
            final HashSet<String> embeddedPlugins = (embeddedCorePluginsFile == null)
                    ? new HashSet<>()
                    : new HashSet<>(Arrays.asList(IOUtils.toString(embeddedCorePluginsFile).split("\n")));

            for (String s : embeddedPlugins) {
                Class<?> cls = Class.forName(s);
                embeddedCorePluginClasses.add(cls);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AllCore() {
        LOGGER.info("Instantiating AllCore");

        for (Class<?> cls : embeddedCorePluginClasses) {
            try {
                embeddedCorePluginInstances.add((IFMLLoadingPlugin) cls.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        List<String> classes = new ArrayList<>();
        for (IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            Collections.addAll(classes, p.getASMTransformerClass());
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
        for (IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            p.injectData(data);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return "io.github.legacymoddingmc.unimixins.all.AllCore$CombinedAccessTransformer";
    }

    public static class CombinedAccessTransformer implements IClassTransformer {
        private final List<IClassTransformer> delegates = new ArrayList<>();
        public CombinedAccessTransformer() {
            for (IFMLLoadingPlugin plugin : embeddedCorePluginInstances) {
                String atClass = plugin.getAccessTransformerClass();
                if (atClass != null) {
                    try {
                        delegates.add((IClassTransformer) Class.forName(atClass).newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            for (IClassTransformer delegate : delegates) {
                basicClass = delegate.transform(name, transformedName, basicClass);
            }
            return basicClass;
        }

        @Override
        public String toString() {
            return delegates.toString();
        }
    }

}
