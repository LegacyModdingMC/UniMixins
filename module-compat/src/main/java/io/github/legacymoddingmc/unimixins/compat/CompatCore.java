package io.github.legacymoddingmc.unimixins.compat;

import java.util.*;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.common.abstraction.ComparableVersion;
import io.github.legacymoddingmc.unimixins.common.config.ConfigUtil;
import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;
import io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

@MCVersion("1.7.10")
public class CompatCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    public CompatCore() {
        LOGGER.info("Instantiating CompatCore");

        ConfigUtil.load(CompatConfig.class);

        if(isLegacyGTNHMixinExtrasEnabled()) {
            Launch.classLoader.registerTransformer(relativeClassName("asm.LegacyGTNHMixinExtrasGenerator"));
            try {
                Class.forName("com.gtnewhorizon.mixinextras.MixinExtrasBootstrap").getMethod("init").invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize MixinExtrasBootstrap");
            }
        }
        if(CompatConfig.enableRemapper) {
            // We register the transformer this way to register it as early as possible.
            Launch.classLoader.registerTransformer(relativeClassName("asm.ASMRemapperTransformer"));
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        List<String> classes = new ArrayList<>();
        if(CompatConfig.enhanceCrashReports) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.EnhanceCrashReportsTransformer");
        }
        if(IgnoreDuplicateJarsTransformer.wantsToRun()) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer");
        }
        if(Boolean.parseBoolean(System.getProperty("unimixins.compat.hackClasspathModDiscovery", "false"))) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.HackClasspathModDiscoveryTransformer");
        }
        return classes.toArray(new String[0]);
    }

    public static boolean isLegacyGTNHMixinExtrasEnabled() {
        if(!CompatConfig.enableLegacyGTNHMixinExtrasPackage) return false;

        String requiredVersion = "0.8.5";
        String mixinVersion = (String)Launch.blackboard.get("mixin.initialised");
        if(mixinVersion != null && new ComparableVersion(mixinVersion).compareTo(new ComparableVersion(requiredVersion)) >= 0) {
            LOGGER.debug("Initializing MixinExtras");
            return true;
        } else if(!SanityCheckHelper.isEnabled()){
            LOGGER.warn("Skipping MixinExtras because Mixin version (" + mixinVersion + ") is lower than the required (" + requiredVersion + ")");
            return false;
        } else {
            throw new RuntimeException("Cannot load MixinExtras because Mixin version (" + mixinVersion + ") is lower than the required (" + requiredVersion + ")");
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

    /**
     * <p>This is the earliest point after the Mixin environment's initialization where we can inject code.
     * <p>We register our error handler here.
     */
    @Override
    public void injectData(Map<String, Object> data) {
        if(CompatConfig.enhanceCrashReports) {
            Mixins.registerErrorHandlerClass("io.github.legacymoddingmc.unimixins.compat.MixinErrorHandler");
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}
