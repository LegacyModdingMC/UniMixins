package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import com.google.common.primitives.Bytes;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>Some classes use Mixin internals, so we need to make sure they use the correct type names, and remap them if they don't.</p>
 * <p>We do this for every class for maximum compatibility.</p>
 */

// TODO Measure the performance impact of this and switch to doing it selectively if it would improve things.
public class ASMRemapperTransformer implements IClassTransformer {

    private static final List<String> ASM_PACKAGE_PREFIXES = Arrays.asList(
            "org/spongepowered/libraries/org/objectweb/asm/",
            "org/spongepowered/asm/lib/"
    );

    private static final byte[] PATTERN = "spongepowered".getBytes(StandardCharsets.UTF_8);

    private static String realASMPackagePrefix;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) {
            return null;
        }
        if(transformedName.startsWith("io.github.legacymoddingmc.unimixins.compat.asm.")
            || transformedName.startsWith("com.google.")
            || transformedName.startsWith("org.apache.")
            || transformedName.startsWith("org.objectweb.asm.")
        ) return basicClass;

        if(Bytes.indexOf(basicClass, PATTERN) == -1) return basicClass;

        ClassReader classReader = new ClassReader(basicClass);

        ClassNode classNode = new ClassNode();
        ClassReader classReaderForNode = new ClassReader(basicClass);
        classReaderForNode.accept(classNode, 0);

        boolean doRemap = Bytes.indexOf(basicClass, getFakeASMPackagePrefix().getBytes()) != -1;

        if(doRemap) {
            LOGGER.info("Transforming class " + transformedName + " to fit current mixin environment.");
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            RemappingClassAdapter remapAdapter = new SpongepoweredASMRemappingAdapter(classWriter);
            classReader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
            basicClass = classWriter.toByteArray();
        }
        return basicClass;
    }

    private static String getRealASMPackagePrefix() {
        if(realASMPackagePrefix == null) {
            try {
                ClassReader cr = new ClassReader(Launch.classLoader.getClassBytes("org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin"));
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);

                for (MethodNode m : cn.methods) {
                    if (m.name.equals("preApply")) {
                        int classNodeDescStart = StringUtils.ordinalIndexOf(m.desc, "L", 2);
                        int classNodeDescEnd = StringUtils.ordinalIndexOf(m.desc, "L", 3);
                        String classNodeName = m.desc.substring(classNodeDescStart + 1, classNodeDescEnd - 1);
                        realASMPackagePrefix = classNodeName.substring(0, classNodeName.indexOf("tree/ClassNode"));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to determine real package name Mixin's shaded ASM.");
            } finally {
                if(realASMPackagePrefix == null) {
                    realASMPackagePrefix = "UNKNOWN";
                }

                LOGGER.debug("Resolved real package prefix to: " + realASMPackagePrefix);
            }
        }
        return realASMPackagePrefix;
    }

    private static String getFakeASMPackagePrefix() {
        return ASM_PACKAGE_PREFIXES.get(ASM_PACKAGE_PREFIXES.get(0).equals(getRealASMPackagePrefix()) ? 1 : 0);
     }

    private static class SpongepoweredASMRemappingAdapter extends RemappingClassAdapter {
        public SpongepoweredASMRemappingAdapter(ClassWriter classWriter) {
            super(classWriter, SpongepoweredASMRemapper.INSTANCE);
        }
    }

    private static class SpongepoweredASMRemapper extends Remapper {

        public static final Remapper INSTANCE = new SpongepoweredASMRemapper();

        @Override
        public String map(String typeName) {
            for(String s : ASM_PACKAGE_PREFIXES) {
                if(typeName.startsWith(s)) {
                    String newName = ASMRemapperTransformer.getRealASMPackagePrefix() + typeName.substring(s.length());
                    return newName;
                }
            }
            return super.map(typeName);
        }
    }
}
