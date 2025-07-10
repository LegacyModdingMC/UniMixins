package com.gtnewhorizon.gtnhmixins.builders;

import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

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
