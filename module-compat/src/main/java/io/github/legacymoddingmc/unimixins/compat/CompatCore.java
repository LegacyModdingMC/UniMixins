package io.github.legacymoddingmc.unimixins.compat;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraft.launchwrapper.Launch;
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
        return new String[] {};
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
