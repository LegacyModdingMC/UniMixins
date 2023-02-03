package com.gtnewhorizon.gtnhmixinslite.core;

import static com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore.LOGGER;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.Launch;

public class GTNHMixinsLite {

    public static void init() {
        Launch.classLoader.registerTransformer("com.gtnewhorizon.gtnhmixinslite.core.asm.MixinExtrasTransformer");
        checkComponentIntegrity();
    }

    private static void checkComponentIntegrity() {
        if(Boolean.parseBoolean(System.getProperty("gtnhmixinslite.skipIntegrityChecks", "false"))) {
            return;
        }

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
            LOGGER.error("Please obtain mods which provide them. See the readme inside the mod jar or the mod's GitHub page for more details.");
            throw new RuntimeException("Missing components detected");
        }
    }

    public static boolean classExists(String string) {
        return GTNHMixinsLite.class.getResource("/" + string.replaceAll("\\.", "/") + ".class") != null;
    }
    
    

}
