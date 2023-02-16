package io.github.legacymoddingmc.unimixins.common;

import net.minecraft.launchwrapper.Launch;

import java.io.File;

public class ConfigUtil {

    public static void load(Class<?> cls) {
        File oldConfigFile = new File("config/unimixins.cfg");
        if(oldConfigFile.exists()) {
            oldConfigFile.delete();
        }

        AnnotatedProperties.load(new File(Launch.minecraftHome, "config/unimixins.properties"), cls);
    }

}
