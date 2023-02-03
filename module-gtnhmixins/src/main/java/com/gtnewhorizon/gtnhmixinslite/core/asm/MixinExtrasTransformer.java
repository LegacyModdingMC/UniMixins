package com.gtnewhorizon.gtnhmixinslite.core.asm;

import com.gtnewhorizon.gtnhmixinslite.core.GTNHMixinsLite;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import static com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore.LOGGER;

/**
 * <p>MixinExtras uses Mixin internals, so we need to make sure it uses the correct type names, and remap it if it doesn't.</p>
 */
// TODO Ideally this should also be done for every mod class (FalsePatternLib does it for mixin plugins already.)
// But it doesn't seem very important, and I don't want to risk breaking anything.
public class MixinExtrasTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.startsWith("com.gtnewhorizon.mixinextras.")) {
            basicClass = doRemap(transformedName, basicClass);
        }
        return basicClass;
    }

    private byte[] doRemap(String transformedName, byte[] bytes) {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        RemappingClassAdapter remapAdapter = new SpongepoweredASMRemappingAdapter(classWriter);
        classReader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static class SpongepoweredASMRemappingAdapter extends RemappingClassAdapter {
        public SpongepoweredASMRemappingAdapter(ClassWriter classWriter) {
            super(classWriter, SpongepoweredASMRemapper.INSTANCE);
        }
    }

    private static class SpongepoweredASMRemapper extends Remapper {

        public static final Remapper INSTANCE = new SpongepoweredASMRemapper();

        private boolean doRemap;

        private static final String GTNHMIXINS_ASM_PACKAGE_PREFIX = "org/spongepowered/libraries/org/objectweb/asm/";
        private static final String GASSTATION_ASM_PACKAGE_PREFIX = "org/spongepowered/asm/lib/";

        public SpongepoweredASMRemapper() {
            doRemap = !GTNHMixinsLite.classExists("org.spongepowered.libraries.org.objectweb.asm.Opcodes");
            if(doRemap) {
                LOGGER.debug("Remapping types in MixinExtras from " + GTNHMIXINS_ASM_PACKAGE_PREFIX + "* to " + GASSTATION_ASM_PACKAGE_PREFIX + "*");
            }
        }

        @Override
        public String map(String typeName) {
            if(doRemap && typeName.startsWith(GTNHMIXINS_ASM_PACKAGE_PREFIX)) {
                String newName = GASSTATION_ASM_PACKAGE_PREFIX + typeName.substring(GTNHMIXINS_ASM_PACKAGE_PREFIX.length());
                return newName;
            }
            return super.map(typeName);
        }
    }
}
