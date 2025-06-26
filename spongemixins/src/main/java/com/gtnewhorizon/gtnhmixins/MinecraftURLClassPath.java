package com.gtnewhorizon.gtnhmixins;

import com.google.common.io.Files;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public final class MinecraftURLClassPath {
    /**
     *  Utility to manipulate the minecraft URL ClassPath 
     */
    private static final Path MOD_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();

    /**
     * Get a jar within the minecraft mods directory
     */
    @SuppressWarnings("All")
    public static File getJarInModPath(final String jarname) {
        try {
            return java.nio.file.Files.walk(MOD_DIRECTORY_PATH)
                .filter( p -> {
                    final String filename = p.toString();
                    final String extension = Files.getFileExtension(filename);
                    
                    return Files.getNameWithoutExtension(filename).contains(jarname)  && ("jar".equals(extension) || "litemod".equals(extension));
                })
                .map(Path::toFile)
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns true if the given mod is found within the class path; generally useful for identifying if a mod has been loaded
     * while running in dev due to a compile dependency 
     */
    @SuppressWarnings("All")
    public static boolean findJarInClassPath(final String jarname) {
        for(URL url : Launch.classLoader.getURLs()) {
            final String filename = url.getFile();
            final String extension = Files.getFileExtension(filename);
            
            if(Files.getNameWithoutExtension(filename).contains(jarname) && ("jar".equals(extension) || "litemod".equals(extension))) {
                return true;
            }
        }
        return false;
    }    

    /**
     * Adds a Jar to the Minecraft URL ClassPath
     *  - Needed when using mixins on classes outside of Minecraft or other coremods 
     */
    public static void addJar(File pathToJar) throws Exception {
        final LaunchClassLoader loader = Launch.classLoader;
        loader.addURL(pathToJar.toURI().toURL());
        // Act as-if we only added the mod to ucp
        loader.getSources().remove(loader.getSources().size() - 1);
    }

    private MinecraftURLClassPath() {
    }


}
