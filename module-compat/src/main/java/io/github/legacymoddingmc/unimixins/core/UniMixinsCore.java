package io.github.legacymoddingmc.unimixins.core;

import static io.github.legacymoddingmc.unimixins.core.Constants.LOGGER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.core.asm.ASMRemapperTransformer;
import net.minecraft.launchwrapper.Launch;

@MCVersion("1.7.10")
public class UniMixinsCore implements IFMLLoadingPlugin {

    public UniMixinsCore() {
        LOGGER.info("Instantiating CoreTweaksPlugin");

        // We register the transformer this way to register it as early as possible.
        Launch.classLoader.registerTransformer(relativeClassName("asm.ASMRemapperTransformer"));
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {};
    }

    private static String relativeClassName(String relName) {
        String name = UniMixinsCore.class.getName();
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
