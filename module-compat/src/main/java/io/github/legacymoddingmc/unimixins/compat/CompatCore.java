package io.github.legacymoddingmc.unimixins.compat;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class CompatCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    public CompatCore() {
        LOGGER.info("Instantiating CompatCore");

        // We register the transformer this way to register it as early as possible.
        Launch.classLoader.registerTransformer(relativeClassName("asm.ASMRemapperTransformer"));
    }

    @Override
    public String[] getASMTransformerClass() {
        applyLWJGL3ifyCompatibilityHack();
        return new String[] {};
    }

    /** LWJGL3ify excludes transformation of its core package, which prevents us from remapping its
     * LwjglRedirectTransformer. However, the exception appears to be unnecessary, since its transformers already
     * have checks that prevent ClassCircularityErrors.
     */
    private static void applyLWJGL3ifyCompatibilityHack() {
        try {
            Field transformerExceptionsF = LaunchClassLoader.class.getDeclaredField("transformerExceptions");
            transformerExceptionsF.setAccessible(true);
            Set<String> transformerExceptions = (Set<String>)transformerExceptionsF.get(Launch.classLoader);
            final String lwjgl3ifyCorePackage = "me.eigenraven.lwjgl3ify.core";
            if(transformerExceptions.contains(lwjgl3ifyCorePackage)) {
                LOGGER.info("Removing '" + lwjgl3ifyCorePackage + "' transformer exclusion to apply LWJGL3ify compatibility hack.");
                transformerExceptions.remove(lwjgl3ifyCorePackage);
            }
        } catch(Exception e){
            LOGGER.debug("Encountered exception while trying to apply LWJGL3ify compatibility hack: " + e);
        }
    }
    private static String relativeClassName(String relName) {
        String name = CompatCore.class.getName();
        name = name.substring(0, name.lastIndexOf('.') + 1);
        name += relName;
        return name;
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
