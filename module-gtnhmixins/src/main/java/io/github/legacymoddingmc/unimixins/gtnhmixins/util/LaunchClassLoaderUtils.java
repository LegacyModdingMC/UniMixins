package io.github.legacymoddingmc.unimixins.gtnhmixins.util;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class LaunchClassLoaderUtils {
    private static Map<String,byte[]> resourceCache;
    private static Set<String> negativeResourceCache;

    static {
        try {
            Field resourceCacheField = LaunchClassLoader.class.getDeclaredField("resourceCache");
            resourceCacheField.setAccessible(true);
            resourceCache = (Map<String, byte[]>) resourceCacheField.get(Launch.classLoader);

            Field negativeResourceCacheField = LaunchClassLoader.class.getDeclaredField("negativeResourceCache");
            negativeResourceCacheField.setAccessible(true);
            negativeResourceCache = (Set<String>) negativeResourceCacheField.get(Launch.classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** Use with care, ArchaicFix weakens the resource cache once it receives the LoadComplete event. */
    public static void putInResourceCache(String key, byte[] value) {
        resourceCache.put(key, value);
        negativeResourceCache.remove(key);
    }
}
