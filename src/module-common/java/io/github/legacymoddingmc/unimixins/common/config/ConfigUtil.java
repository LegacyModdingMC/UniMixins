package io.github.legacymoddingmc.unimixins.common.config;

import net.minecraft.launchwrapper.Launch;

import java.io.File;

public class ConfigUtil {

    public static void load(Class<?> cls) {
        File configDir = new File(Launch.minecraftHome, "config");
        File oldConfigFile = new File(configDir, "unimixins.cfg");
        if(oldConfigFile.exists()) {
            oldConfigFile.renameTo(new File(configDir, "unimixins.old.delete_me.cfg"));
        }

        AnnotatedProperties.load(new File(configDir, "unimixins.properties"), cls);
    }

}
