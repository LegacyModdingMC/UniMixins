package ru.timeconqueror.spongemixins.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import sun.misc.URLClassPath;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 5)
@IFMLLoadingPlugin.Name(SpongeMixinsCore.PLUGIN_NAME)
public class SpongeMixinsCore implements IFMLLoadingPlugin {
    public static final String PLUGIN_NAME = "SpongeMixin Core Plugin";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_NAME);

    static {
        LOGGER.info("Initializing SpongeMixinsCore");
        fixMixinClasspathOrder();
        MixinBootstrap.init();
    }

    private static void fixMixinClasspathOrder() {
        // Borrowed from VanillaFix -- Move jar up in the classloader's URLs to make sure that the latest version of Mixin is used
        URL url = SpongeMixinsCore.class.getProtectionDomain().getCodeSource().getLocation();
        givePriorityInClasspath(url, Launch.classLoader);
        givePriorityInClasspath(url, (URLClassLoader) ClassLoader.getSystemClassLoader());
    }

    private static void givePriorityInClasspath(URL url, URLClassLoader classLoader) {
        try {
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);

            List<URL> urls = new ArrayList<>(Arrays.asList(classLoader.getURLs()));
            urls.remove(url);
            urls.add(0, url);
            URLClassPath ucp = new URLClassPath(urls.toArray(new URL[0]));

            ucpField.set(classLoader, ucp);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
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

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

