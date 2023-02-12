package io.github.legacymoddingmc.unimixins.mixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;;
import java.util.*;

/**
 * This plugin only exists to make Mixin add the MixinPlatformAgentFMLLegacy agent for the primary mixin container,
 * which is where we run the Forge-specific init code.
 */

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class MixinCore implements IFMLLoadingPlugin {

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
