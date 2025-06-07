package io.github.legacymoddingmc.unimixins.mixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Handles platform-specific setup of the Mixin environment.
 */

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class MixinCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    static {
        if(SanityCheckHelper.isEnabled()) {
            SanityCheckHelper.showBigWarning(
                MixinSanityCheck.checkMixinHasInitialized(),
                MixinSanityCheck.checkMixinContainer(),
                SanityCheckHelper.checkIfJarPrefixesExist(Arrays.asList("gasstation-", "mixinbooterlegacy-", "spongemixins-"))
            );
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
