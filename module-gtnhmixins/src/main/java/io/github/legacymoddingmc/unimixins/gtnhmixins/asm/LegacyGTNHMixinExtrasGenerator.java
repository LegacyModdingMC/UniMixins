package io.github.legacymoddingmc.unimixins.gtnhmixins.asm;

import static io.github.legacymoddingmc.unimixins.gtnhmixins.GTNHMixinsModule.LOGGER;

import io.github.legacymoddingmc.unimixins.gtnhmixins.util.LaunchClassLoaderUtils;
import makamys.mixingasm.api.MixinSafeTransformer;
import makamys.mixingasm.api.TransformerInclusions;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

@MixinSafeTransformer
public class LegacyGTNHMixinExtrasGenerator implements IClassTransformer {
    static {
        if(LegacyGTNHMixinExtrasGenerator.class.getResource("/makamys/mixingasm/api/TransformerInclusions.class") != null
        && LegacyGTNHMixinExtrasGenerator.class.getResource("/makamys/mixingasm/api/MixinSafeTransformer.class") == null) {
            // Mixingasm < 0.3 compat
            TransformerInclusions.getTransformerInclusionList().add("io.github.legacymoddingmc.unimixins.compat.asm.LegacyGTNHMixinExtrasGenerator");
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null && name.startsWith("com.gtnewhorizon.mixinextras") && !name.startsWith("org.apache.commons.io.")) {
            try {
                LOGGER.trace("Generating class " + name);
                byte[] bytes = getClassBytes(name);

                if(bytes != null) {
                    // If NEI is on the class path before CCL (which would cause the game to crash immediately in
                    // production, but is possible in a dev env), ClassHeirachyTransformer will try to call getClassBytes,
                    // and if it fails, it will call Class.forName for the class being loaded, causing a
                    // ClassCircularityError.
                    // So we need to populate the cache to avoid that from happening.
                    LaunchClassLoaderUtils.putInResourceCache(name, bytes);
                    return bytes;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate class " + name);
            }
        }
        return basicClass;
    }

    private byte[] getClassBytes(String name) throws IOException {
        final String resourcePath = "/data/gtnhmixinextras/" + name.replace('.', '/').concat(".klass");
        return IOUtils.toByteArray(LegacyGTNHMixinExtrasGenerator.class.getResourceAsStream(resourcePath));
    }
}
