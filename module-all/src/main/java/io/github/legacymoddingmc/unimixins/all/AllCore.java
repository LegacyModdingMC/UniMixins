package io.github.legacymoddingmc.unimixins.all;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class AllCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    private static List<Class<?>> embeddedCorePluginClasses = new ArrayList<>();
    private static List<IFMLLoadingPlugin> embeddedCorePluginInstances = new ArrayList<>();

    static {
        try {
            for(String s : IOUtils.toString(AllCore.class.getResource("/META-INF/unimixins-all.EmbeddedFMLCorePlugins.txt")).split(" ")) {
                Class<?> cls = Class.forName(s);
                embeddedCorePluginClasses.add(cls);
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
