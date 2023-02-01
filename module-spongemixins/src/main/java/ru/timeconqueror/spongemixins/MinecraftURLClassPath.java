package ru.timeconqueror.spongemixins;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import com.google.common.io.Files;
import sun.misc.URLClassPath;

public final class MinecraftURLClassPath {
    /**
     *  Utility to manipulate the minecraft URL ClassPath 
     */
    private static final Path MOD_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();
    private static final Field modClassLoaderField;
    private static final Field loaderinstanceField;
    private static final Field mainClassLoaderField;
    private static final Field ucpField;
    private static final ModClassLoader modClassLoader;
    private static final LaunchClassLoader mainClassLoader;
    private static final URLClassPath ucp;

    static {
        try {
            modClassLoaderField = Loader.class.getDeclaredField("modClassLoader");
            modClassLoaderField.setAccessible(true);

            loaderinstanceField = Loader.class.getDeclaredField("instance");
            loaderinstanceField.setAccessible(true);
            
            mainClassLoaderField = ModClassLoader.class.getDeclaredField("mainClassLoader");
            mainClassLoaderField.setAccessible(true);

            ucpField = LaunchClassLoader.class.getSuperclass().getDeclaredField("ucp");
            ucpField.setAccessible(true);
            
            Object loader = loaderinstanceField.get(null);
            modClassLoader = (ModClassLoader)modClassLoaderField.get(loader);
            mainClassLoader = (LaunchClassLoader)mainClassLoaderField.get(modClassLoader);
            ucp = (URLClassPath)ucpField.get(mainClassLoader);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

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
        for(URL url : ucp.getURLs()) {
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
        ucp.addURL(pathToJar.toURI().toURL());
    }

    private MinecraftURLClassPath() {
    }


}
