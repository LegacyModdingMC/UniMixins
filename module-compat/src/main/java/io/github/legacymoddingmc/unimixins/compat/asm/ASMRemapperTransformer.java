package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
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
    private static final String REMAP_ANNOTATION = "Lio/github/legacymoddingmc/unimixins/compat/api/RemapASMForMixin;";

    private static final List<String> ASM_PACKAGE_PREFIXES = Arrays.asList(
            ASM_PACKAGE_UNSHADED,
            ASM_PACKAGE_LEGACY,
            ASM_PACKAGE_MBL
    );

    private static String realASMPackagePrefix;
    private final BytePatternMatcher wrongAsmMatcher;
    private final BytePatternMatcher shadedAsmMatcher;
    private final BytePatternMatcher remapAnnotationMatcher;

    public ASMRemapperTransformer() {
        final String[] wrongAsmPackagePrefixes = ASM_PACKAGE_PREFIXES.stream()
                .filter(x -> !x.equals(getRealASMPackagePrefix()))
                .toArray(String[]::new);

        final String[] shadedAsmPackagePrefixes = new String[] { ASM_PACKAGE_LEGACY, ASM_PACKAGE_MBL };

        this.wrongAsmMatcher = new BytePatternMatcher(wrongAsmPackagePrefixes, BytePatternMatcher.Mode.Contains);
        this.shadedAsmMatcher = new BytePatternMatcher(shadedAsmPackagePrefixes, BytePatternMatcher.Mode.Contains);
        this.remapAnnotationMatcher = new BytePatternMatcher(REMAP_ANNOTATION, BytePatternMatcher.Mode.Equals);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (transformedName.startsWith("io.github.legacymoddingmc.unimixins.compat.asm.")
            || transformedName.startsWith("com.google.")
            || transformedName.startsWith("org.apache.")
            || transformedName.startsWith("org.objectweb.asm.")
        ) {
            return basicClass;
        }

        ClassReader classReader = new ClassReader(basicClass);

        boolean foundWrongAsm = matchUtf8Constant(classReader, basicClass, wrongAsmMatcher);
        if (!foundWrongAsm) return basicClass;

        boolean doRemap = matchUtf8Constant(classReader, basicClass, shadedAsmMatcher);

        if (!doRemap) {
            for (String itf : classReader.getInterfaces()) {
                if (itf.equals("org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin")) {
                    doRemap = true;
                    break;
                }
            }
        }

        if (!doRemap) {
            doRemap = matchUtf8Constant(classReader, basicClass, remapAnnotationMatcher);
        }

        if (!doRemap) {
            return basicClass;
        }

        LOGGER.info("Transforming class {} to fit current mixin environment.", transformedName);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        RemappingClassAdapter remapAdapter = new SpongepoweredASMRemappingAdapter(classWriter);
        classReader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static boolean matchUtf8Constant(ClassReader classReader, byte[] basicClass, BytePatternMatcher matcher) {
        final int itemCount = classReader.getItemCount();

        // Constant pool entries start from index 1
        for (int i = 1; i < itemCount; i++) {
            final int itemOffset = classReader.getItem(i);

            // Long and double take two CP slots, the second slot has no item
            if (itemOffset == 0) {
                continue;
            }

            // Only match the UTF8 constant (which tag is 1).
            // getItem(i) points after the tag byte, i.e. at the first length byte.
            // [tag][u2 length][bytes...]
            if (basicClass[itemOffset - 1] != 1) {
                continue;
            }

            // Length takes 2 bytes, so we read them and skip these 2 bytes
            final int utfLen = classReader.readUnsignedShort(itemOffset);
            final int utfStart = itemOffset + 2;

            if (matcher.matches(basicClass, utfStart, utfLen)) {
                return true;
            }
        }

        return false;
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
