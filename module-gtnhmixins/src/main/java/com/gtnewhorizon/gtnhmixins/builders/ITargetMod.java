package com.gtnewhorizon.gtnhmixins.builders;

import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This interface is used to identify mods targeted by the {@link com.gtnewhorizon.gtnhmixins.builders.IMixins}
 * and {@link com.gtnewhorizon.gtnhmixins.builders.ITransformers} system. It provides a flexible mechanism for
 * conditionally applying mixins or asm transformers based on the presence or absence of other mods during runtime.
 * You can declare your targeted mods in an enum, although it's not required to be an enum.
 * <pre>
 * {@code
 * public enum TargetMods implements ITargetMod {
 *
 *     COFHCORE("cofh.asm.LoadingPlugin", "CoFHCore"),
 *     OPTIFINE("optifine.OptiFineForgeTweaker", "Optifine");
 *
 *     private final TargetModBuilder builder;
 *
 *     TargetMods(String coreModClass, String modId) {
 *         this.builder = new TargetModBuilder().setCoreModClass(coreModClass).setModId(modId);
 *     }
 *
 *     @Override
 *     public getBuidler() {
 *         return builder;
 *     }
 * }
 * }
 * </pre>
 */
public interface ITargetMod {

    @Nonnull
    TargetModBuilder getBuilder();

    class Predicates {

        public static Predicate<String> equals(String str) {
            return s -> s.equals(str);
        }

        public static Predicate<String> contains(String str) {
            return s -> s.contains(str);
        }

        public static Predicate<String> endsWith(String str) {
            return s -> s.endsWith(str);
        }

        public static Predicate<String> startsWith(String str) {
            return s -> s.startsWith(str);
        }

        public static Predicate<String> matches(String str) {
            return s -> s.matches(str);
        }

        /**
         * Returns a predicate that is true if the version provided is greater than the version it is compared to.
         */
        public static Predicate<String> versionGreater(String version) {
            return s -> new ComparableVersion(version).compareTo(new ComparableVersion(s)) > 0;
        }

        /**
         * Returns a predicate that is true if the version provided is lower than the version it is compared to.
         */
        public static Predicate<String> versionLower(String version) {
            return s -> new ComparableVersion(version).compareTo(new ComparableVersion(s)) < 0;
        }

        /**
         * Returns a ClassNode test that will return true if the targeted class contains a method with a matching name.
         */
        public static Predicate<ClassNode> hasMethod(@Nonnull String name) {
            Objects.requireNonNull(name);
            return cn -> {
                final int size = cn.methods.size();
                for (int i = 0; i < size; i++) {
                    MethodNode mn = cn.methods.get(i);
                    if (name.equals(mn.name)) {
                        return true;
                    }
                }
                return false;
            };
        }

        /**
         * Returns a ClassNode test that will return true if the targeted class contains a method with a matching name and descriptor.
         */
        public static Predicate<ClassNode> hasMethod(@Nonnull String name, @Nonnull String desc) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(desc);
            return cn -> {
                final int size = cn.methods.size();
                for (int i = 0; i < size; i++) {
                    MethodNode mn = cn.methods.get(i);
                    if (name.equals(mn.name) && desc.equals(mn.desc)) {
                        return true;
                    }
                }
                return false;
            };
        }

        /**
         * Returns a ClassNode test that will test the {@link cpw.mods.fml.common.Mod} annotation - if present - of the targeted class against the passed in Predicates.
         * At least one of the predicates must be non-null, this test will return true if the targeted
         * class has a {@link cpw.mods.fml.common.Mod} annotation and if all the predicates are true.
         */
        public static Predicate<ClassNode> testModAnnotation(Predicate<String> modIdTest, Predicate<String> modNameTest, Predicate<String> modVersionTest) {
            if (modIdTest == null && modNameTest == null && modVersionTest == null) {
                throw new IllegalArgumentException("At least one of the Mod Annotation test predicates must be non null");
            }
            return cn -> {
                if (cn.visibleAnnotations == null) return false;
                for (AnnotationNode annotation : cn.visibleAnnotations) {
                    if ("Lcpw/mods/fml/common/Mod;".equals(annotation.desc)) {
                        if (annotation.values != null) {
                            String modId = "";
                            String modName = "";
                            String modVersion = "";
                            final List<Object> values = annotation.values;
                            final int size = values.size();
                            for (int i = 0; i < size - 1; i += 2) {
                                Object name = values.get(i);
                                Object value = values.get(i + 1);
                                if (name instanceof String && value instanceof String) {
                                    if ("modid".equals(name)) {
                                        modId = (String) value;
                                    } else if ("name".equals(name)) {
                                        modName = (String) value;
                                    } else if ("version".equals(name)) {
                                        modVersion = (String) value;
                                    }
                                }
                            }
                            boolean test = true;
                            if (modIdTest != null) {
                                test = /*test &&*/ modIdTest.test(modId);
                            }
                            if (modNameTest != null) {
                                test = test && modNameTest.test(modName);
                            }
                            if (modVersionTest != null) {
                                test = test && modVersionTest.test(modVersion);
                            }
                            return test;
                        }
                    }
                }
                return false;
            };
        }
    }
}
