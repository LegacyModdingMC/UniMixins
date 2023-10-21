package io.github.legacymoddingmc.unimixins.devcompat;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class DevCompatCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    public DevCompatCore() {
        LOGGER.info("Instantiating DevCompatCore");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {relativeClassName("asm.ClasspathModDiscoveryFixerTransformer")};
    }

    private static String relativeClassName(String relName) {
        String name = DevCompatCore.class.getName();
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
