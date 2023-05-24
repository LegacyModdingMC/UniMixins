package ru.timeconqueror.spongemixins.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 5)
@IFMLLoadingPlugin.Name(SpongeMixinsCore.PLUGIN_NAME)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 5)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name(SpongeMixinsCore.PLUGIN_NAME)
public class SpongeMixinsCore implements IFMLLoadingPlugin {
    public static final String PLUGIN_NAME = "SpongeMixin Core Plugin";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_NAME);

    static {
        LOGGER.info("Initializing SpongeMixinsCore");
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

