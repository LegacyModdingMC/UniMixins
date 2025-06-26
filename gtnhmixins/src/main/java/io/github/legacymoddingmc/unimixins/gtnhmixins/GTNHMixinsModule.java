package io.github.legacymoddingmc.unimixins.gtnhmixins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.legacymoddingmc.unimixins.common.abstraction.ComparableVersion;
import io.github.legacymoddingmc.unimixins.common.config.ConfigUtil;
import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GTNHMixinsModule {

    public static final Logger LOGGER = LogManager.getLogger("unimixins-gtnhmixins");

    public static void init() {
        if(SanityCheckHelper.isEnabled()) {
            SanityCheckHelper.warnIfJarPrefixesExist(Arrays.asList("gasstation-", "mixinbooterlegacy-", "spongemixins-"));
            checkComponentIntegrity();
        }

        ConfigUtil.load(GTNHMixinsConfig.class);

        if(isLegacyGTNHMixinExtrasEnabled()) {
            Launch.classLoader.registerTransformer("io.github.legacymoddingmc.unimixins.gtnhmixins.asm.LegacyGTNHMixinExtrasGenerator");
            try {
                Class.forName("com.gtnewhorizon.mixinextras.MixinExtrasBootstrap").getMethod("init").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize MixinExtrasBootstrap");
            }
        }
    }

    public static boolean isLegacyGTNHMixinExtrasEnabled() {
        if(!GTNHMixinsConfig.enableLegacyGTNHMixinExtrasPackage) return false;

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

    private static void checkComponentIntegrity() {
        List<String> missingComponents = new ArrayList<>();
        
        // This is actually redundant; without Mixin, the mod can't even load since its tweaker class is missing
        if(!classExists("org.spongepowered.asm.launch.MixinBootstrap")) {
            missingComponents.add("Mixin");
        }
        if(!classExists("ru.timeconqueror.spongemixins.SpongeMixins")) {
            missingComponents.add("SpongeMixins");
        }
        
        if(!missingComponents.isEmpty()) {
            LOGGER.error("The following missing components were detected: " + missingComponents);
            LOGGER.error("Please obtain mods which provide them.");
            throw new RuntimeException("Missing components detected");
        }
    }

    public static boolean classExists(String string) {
        return GTNHMixinsModule.class.getResource("/" + string.replaceAll("\\.", "/") + ".class") != null;
    }
    
    

}
