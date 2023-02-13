package io.github.legacymoddingmc.unimixins.mixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;;
import java.util.*;

/**
 * Handles platform-specific setup of the Mixin environment.
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

    /**
     * This method is the earliest point we can run code after Mixin is initialized via MixinTweaker.
     * @param data
     */
    @Override
    public void injectData(Map<String, Object> data) {
        MixinModidDecorator.apply();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
