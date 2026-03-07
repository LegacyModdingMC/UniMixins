package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

/**
 * <p>The ASM package name used by Mixin differs between mixin loaders. This transformer remaps references to ASM to use the
 * correct package name for the current runtime environment, allowing mods to work regardless of what mixin loader they
 * were compiled against.</p>
 * <p>The following packages are remapped:</p>
 * <li><code>org.spongepowered.libraries.org.objectweb.asm</code> (MixinBooterLegacy, GTNHMixins) in all classes</li>
 * <li><code>org.spongepowered.asm.lib</code> (Mixin 0.7, UniMixins) in all classes</li>
 * <li><code>org.objectweb.asm</code> (Mixin 0.8) in classes implementing {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin} or annotated with {@link io.github.legacymoddingmc.unimixins.compat.api.RemapASMForMixin}</li>
 */

public class ASMRemapperTransformer implements IClassTransformer {

    private static final String ASM_PACKAGE_UNSHADED = "org/objectweb/asm/";
    private static final String ASM_PACKAGE_LEGACY = "org/spongepowered/asm/lib/";
    private static final String ASM_PACKAGE_MBL = "org/spongepowered/libraries/org/objectweb/asm/";

    private static final List<String> ASM_PACKAGE_PREFIXES = Arrays.asList(
            ASM_PACKAGE_UNSHADED,
            ASM_PACKAGE_LEGACY,
            ASM_PACKAGE_MBL
    );

    private static String realASMPackagePrefix;
    private final ClassConstantPoolParser wrongAsmParser;
    private final ClassConstantPoolParser shadedAsmParser;

    public ASMRemapperTransformer() {
        final String[] wrongAsmPackagePrefixes = ASM_PACKAGE_PREFIXES.stream()
                .filter(x -> !x.equals(getRealASMPackagePrefix()))
                .toArray(String[]::new);

        final String[] shadedAsmPackagePrefixes = new String[] { ASM_PACKAGE_LEGACY, ASM_PACKAGE_MBL };

        this.wrongAsmParser = new ClassConstantPoolParser(wrongAsmPackagePrefixes);
        this.shadedAsmParser = new ClassConstantPoolParser(shadedAsmPackagePrefixes);
    }

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

        boolean foundWrongAsm = wrongAsmParser.find(basicClass, true);
        if(!foundWrongAsm) return basicClass;

        boolean foundShadedAsm = shadedAsmParser.find(basicClass, true);
        boolean doRemap = foundShadedAsm;

        if(!doRemap) {
            ClassNode classNode = new ClassNode();
            ClassReader classReaderForNode = new ClassReader(basicClass);
            classReaderForNode.accept(classNode, 0);

            if(classNode.interfaces != null) {
                for (String itf : classNode.interfaces) {
                    if (itf.equals("org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin")) {
                        doRemap = true;
                        break;
                    }
                }
            }
            if(!doRemap && classNode.visibleAnnotations != null) {
                for (AnnotationNode ann : classNode.visibleAnnotations) {
                    if (ann.desc.equals("Lio/github/legacymoddingmc/unimixins/compat/api/RemapASMForMixin;")) {
                        doRemap = true;
                        break;
                    }
                }
            }
        }

        if(doRemap) {
            ClassReader classReader = new ClassReader(basicClass);
            LOGGER.info("Transforming class {} to fit current mixin environment.", transformedName);
            ClassWriter classWriter = new ClassWriter(0);
            RemappingClassAdapter remapAdapter = new SpongepoweredASMRemappingAdapter(classWriter);
            classReader.accept(remapAdapter, 0);
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
