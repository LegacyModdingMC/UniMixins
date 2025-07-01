package com.gtnewhorizon.gtnhmixins.mixinbuilder;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Predicate;

public interface ITargetedMod {

    /**
     * Fully qualified name of the class that implements the IFMLLoadingPlugin interface in this targeted mod. This can
     * only be used by GTNH mixins early mixins system.
     * <p>
     * For example : "com.gtnewhorizons.angelica.loading.AngelicaTweaker"
     */
    default String getCoreModClass() {
        return null;
    }

    /**
     * The "modid" of the targeted mod, found in the @Mod(modid=) annotation. This can only be used by GTNH mixins late
     * mixins system.
     */
    default String getModId() {
        return null;
    }

    /**
     * Fully qualified name of a class of your choice contained in the targeted mod. Typically, its main mod class.
     * <p>
     * For example : "com.gtnewhorizons.angelica.AngelicaMod"
     */
    default String getTargetClass() {
        return null;
    }

    /**
     * A conditional check that will test the raw (un-transformed) bytecode of the class specified in
     * {@link ITargetedMod#getTargetClass()}. Requires you to implement {@link ITargetedMod#getTargetClass()}.
     */
    default Predicate<ClassNode> getClassNodeTest() {
        return null;
    }

    /**
     * A conditional check that will test the name of the jar files. !!! This mod identification method should only be
     * used as a last resort if you cannot identify your targeted mod with the other methods !!!
     */
    default Predicate<String> getJarNameTest() {
        return null;
    }

}
