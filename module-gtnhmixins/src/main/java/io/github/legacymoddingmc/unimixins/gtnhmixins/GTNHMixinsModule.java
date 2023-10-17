package io.github.legacymoddingmc.unimixins.gtnhmixins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.legacymoddingmc.unimixins.common.sanitycheck.SanityCheckHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GTNHMixinsModule {

    private static final Logger LOGGER = LogManager.getLogger("unimixin-gtnhmixins");

    public static void init() {
        if(SanityCheckHelper.isEnabled()) {
            SanityCheckHelper.warnIfJarPrefixesExist(Arrays.asList("gasstation-", "mixinbooterlegacy-", "spongemixins-"));
            checkComponentIntegrity();
        }
    }

    private static void registerASMRemapPackage(String pkg) {
        try {
            Class.forName("io.github.legacymoddingmc.unimixins.compat.asm.ASMRemapperTransformer").getMethod("registerPackage", String.class).invoke(null, pkg);
        } catch(Exception e) {
            LOGGER.warn("Failed to register package " + pkg + " for ASM remapping, probably because the compat module is missing. " + e);
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
