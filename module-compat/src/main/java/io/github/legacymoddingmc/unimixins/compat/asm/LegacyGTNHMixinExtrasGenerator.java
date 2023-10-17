package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import makamys.mixingasm.api.MixinSafeTransformer;
import makamys.mixingasm.api.TransformerInclusions;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

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
        if(name.startsWith("com.gtnewhorizon.mixinextras")) {
            try {
                LOGGER.trace("Generating class " + name);
                return Launch.classLoader.getClassBytes("data.gtnhmixinextras." + name);
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate class " + name);
            }
        } else {
            return basicClass;
        }
    }
}
