package io.github.legacymoddingmc.unimixins.core.asm;

import static io.github.legacymoddingmc.unimixins.core.Constants.LOGGER;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

/**
 * <p>Some classes use Mixin internals, so we need to make sure they uses the correct type names, and remap them if they don't.</p>
 */
// Ideally this should be done for every mod class, but we do it selectively out of caution.
// TODO Test if doing this for every class would affect performance or cause other issues.
public class ASMRemapperTransformer implements IClassTransformer {

    /** A class will be remapped if it implements one of these interfaces. */
    private static Set<String> interfaceWhitelist = new HashSet<>();

    private static final List<String> ASM_PACKAGE_PREFIXES = Arrays.asList(
            "org/spongepowered/libraries/org/objectweb/asm/",
            "org/spongepowered/asm/lib/"
    );

    private static String realASMPackagePrefix;

    public static void registerInterface(String itf) {
        interfaceWhitelist.add(itf.replace('.', '/'));
    }

    public ASMRemapperTransformer() {
        registerInterface("org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) {
            return null;
        }
        ClassReader classReader = new ClassReader(basicClass);

        ClassNode classNode = new ClassNode();
        ClassReader classReaderForNode = new ClassReader(basicClass);
        classReaderForNode.accept(classNode, 0);

        boolean doRemap = false;

        for(String itf : classNode.interfaces) {
            LOGGER.trace("found interface: " + itf);
            if(interfaceWhitelist.contains(itf)) {
                LOGGER.trace("matched by whitelist entry");
                doRemap = true;
            }
        }

        if(doRemap) {
            LOGGER.info("Transforming class " + transformedName + " to fit current Mixin environment.");
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            RemappingClassAdapter remapAdapter = new SpongepoweredASMRemappingAdapter(classWriter);
            classReader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
            return classWriter.toByteArray();
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
