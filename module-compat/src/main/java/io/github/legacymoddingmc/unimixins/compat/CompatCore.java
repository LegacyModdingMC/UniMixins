package io.github.legacymoddingmc.unimixins.compat;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

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
        return new String[] {"io.github.legacymoddingmc.unimixins.compat.asm.FMLCommonHandlerTransformer"};
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

    /**
     * <p>This is the earliest point after the Mixin environment's initialization where we can inject code.
     * <p>We register our error handler here.
     */
    @Override
    public void injectData(Map<String, Object> data) {
        Mixins.registerErrorHandlerClass("io.github.legacymoddingmc.unimixins.compat.MixinErrorHandler");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}
