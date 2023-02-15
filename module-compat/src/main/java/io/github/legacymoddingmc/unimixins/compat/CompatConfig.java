package io.github.legacymoddingmc.unimixins.compat;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class CompatConfig {
    private static final Properties PROPS = new Properties();

    public static boolean enableRemapper;
    public static boolean enhanceCrashReports;

    public static void load() {
        try(FileReader fr = new FileReader(new File(Launch.minecraftHome, "config/unimixins.properties"))) {
            PROPS.load(fr);
        } catch(IOException e){
            e.printStackTrace();
        }

        applyProperties();

        try(FileWriter fw = new FileWriter(new File(Launch.minecraftHome, "config/unimixins.properties"))) {
            PROPS.store(fw, "");
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void applyProperties() {
        enableRemapper = Boolean.parseBoolean(PROPS.getProperty("unimixins.compat.remap", "true"));
        enhanceCrashReports = Boolean.parseBoolean(PROPS.getProperty("unimixins.compat.enhanceCrashReports", "true"));
    }
}
