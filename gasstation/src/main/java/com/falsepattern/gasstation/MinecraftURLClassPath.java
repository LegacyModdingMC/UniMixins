/**
 * Copyright 2020 TimeConqueror
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.falsepattern.gasstation;

import com.google.common.io.Files;
import sun.misc.URLClassPath;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;

public class MinecraftURLClassPath {
    /**
     *  Utility to manipulate the minecraft URL ClassPath
     */
    private static final Path MOD_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();
    private static final URLClassPath ucp;

    static {
        try {
            Field ucpField = LaunchClassLoader.class.getSuperclass().getDeclaredField("ucp");
            ucpField.setAccessible(true);

            ucp = (URLClassPath)ucpField.get(Launch.classLoader);
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
