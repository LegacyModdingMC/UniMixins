package io.github.legacymoddingmc.unimixins.mixinextras;

import java.util.Map;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class MixinExtrasCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins-mixinextras");

    public MixinExtrasCore() {
        LOGGER.info("Instantiating " + getClass().getSimpleName());

        MixinExtrasBootstrap.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {};
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
