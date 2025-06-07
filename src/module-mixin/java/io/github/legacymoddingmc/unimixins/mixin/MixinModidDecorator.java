package io.github.legacymoddingmc.unimixins.mixin;

// Reference: https://github.com/FabricMC/fabric-loader/blob/2a378f1c563b6ec96ae6620a278ecd23fa09da0f/src/main/java/net/fabricmc/loader/impl/launch/FabricMixinBootstrap.java

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Decorates mixin configs with the first modid read from the <tt>mcmod.info</tt> of the enclosing jar, so they can be
 * used in Mixin error messages.
 */
public final class MixinModidDecorator {

    private static final Logger logger = LogManager.getLogger();

    public static final String KEY_MOD_ID = "fabric-modId";

    private static Map<String, String> jarNameToModid;

    private MixinModidDecorator() {}

    public static void apply() {
        // FML has added all mods containing the Mixin tweaker to the class path at this point, so we only need to scan
        // the class path one time.
        if(jarNameToModid == null) {
            jarNameToModid = createJarNameToModidMap();

            // A method handle that can be used anywhere to trigger a refresh. This should be called whenever mixin
            // configs are added later.
            Launch.blackboard.put("unimixins.mixinModidDecorator.refresh", (Runnable)MixinModidDecorator::apply);
        }

        for (Config config : Mixins.getConfigs()) {
            if(!config.getConfig().hasDecoration(KEY_MOD_ID)) {
                URL resource = Launch.classLoader.getResource(config.getName());
                if (resource != null) {
                    String jarName = getJarNameFromResourceUrl(resource);
                    if (jarName != null) {
                        String modid = jarNameToModid.get(jarName);
                        if (modid != null) {
                            config.getConfig().decorate(KEY_MOD_ID, modid);
                        }
                    }
                }
            }
        }
    }

    private static Map<String, String> createJarNameToModidMap() {
        Map<String, String> map = new HashMap<>();
        try {
            for (URL url : Collections.list(Launch.classLoader.getResources("mcmod.info"))) {
                String fileName = getJarNameFromResourceUrl(url);
                if (fileName != null) {
                    List<ModMetadata> metas = parseMcmodInfo(url);
                    if (!metas.isEmpty()) {
                        String modid = metas.get(0).modid;
                        if (modid != null) {
                            map.put(fileName, createUniqueModid(modid, map.values()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to construct jar name -> modid map, mod names will not be shown in errors.");
        }
        return map;
    }

    /**
     * Creates a modid that is unique and is a valid Java identifier. Required because Fabric's Mixin may put the modid
     * in method names.
     */
    private static String createUniqueModid(String modid, Collection<String> registeredModids) {
        String sanitizedModid = "";

        // Replace invalid characters with a underscore
        // Valid characters are the same as in Fabric, but we allow capitalized letters too, and we don't allow dashes
        for(int i = 0; i < modid.length(); i++) {
            char c = modid.charAt(i);
            boolean valid = i == 0 ? isValidLetter(c) : isValidLetter(c) || isValidDigit(c);
            sanitizedModid += valid ? c : '_';
        }
        modid = sanitizedModid;

        // Add a numeric suffix in case of a name conflict
        int discriminator = 1;
        while(registeredModids.contains(modid)) {
            discriminator++;
            modid = sanitizedModid + "_" + discriminator;
        }

        return modid;
    }

    private static boolean isValidLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isValidDigit(char c) {
        return (c >= '0' && c <= '9') || c == '_';
    }

    private static List<ModMetadata> parseMcmodInfo(URL url) {
        try {
            JsonElement root = new Gson().fromJson(new InputStreamReader(url.openStream()), JsonElement.class);
            if (root.isJsonArray()) {
                return Arrays.asList(new Gson().fromJson(new InputStreamReader(url.openStream()), ModMetadata[].class));
            } else {
                return ((ModMetadataCollection)new Gson().fromJson(new InputStreamReader(url.openStream()), ModMetadataCollection.class)).modList;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse mcmod.info at " + url + ": " + e);
        }
        return Arrays.asList();
    }

    private static String getJarNameFromResourceUrl(URL url) {
        if (url.getPath().contains("!/")) {
            String filePath = url.getPath().split("!/")[0];
            String[] parts = filePath.split("/");
            if (parts.length != 0) {
                return parts[parts.length - 1];
            }
        }
        return null;
    }

    private static class ModMetadata {
        String modid;
    }

    private static class ModMetadataCollection {
        List<ModMetadata> modList;
    }

}
