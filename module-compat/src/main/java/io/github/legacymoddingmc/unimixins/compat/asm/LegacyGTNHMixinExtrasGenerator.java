package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;

public class LegacyGTNHMixinExtrasGenerator implements IClassTransformer {
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
