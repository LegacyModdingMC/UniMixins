package com.gtnewhorizon.gtnhmixins.builders;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Predicate;

public class TargetModBuilder {

    private String coreModClass;
    private String modId;
    private String targetClass;
    private Predicate<ClassNode> classNodeTest;
    private Predicate<String> jarNameTest;

    /**
     * Fully qualified name of the class that implements the IFMLLoadingPlugin interface in this targeted mod. This can
     * only be used by GTNH mixins early mixins system.
     * <p>
     * For example : "com.gtnewhorizons.angelica.loading.AngelicaTweaker"
     */
    public TargetModBuilder setCoreModClass(String coreModClass) {
        this.coreModClass = coreModClass;
        return this;
    }

    /**
     * The "modid" of the targeted mod, found in the @Mod(modid=) annotation. This can only be used by GTNH mixins late
     * mixins system.
     */
    public TargetModBuilder setModId(String modId) {
        this.modId = modId;
        return this;
    }

    /**
     * Fully qualified name of a class of your choice contained in the targeted mod. Typically, its main mod class.
     * <p>
     * For example : "com.gtnewhorizons.angelica.AngelicaMod"
     */
    public TargetModBuilder setTargetClass(String targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    /**
     * A conditional check that will test the raw (un-transformed) bytecode of the class specified in
     * {@link TargetModBuilder#setTargetClass}. Requires you to implement {@link TargetModBuilder#setTargetClass}.
     */
    public TargetModBuilder setClassNodeTest(Predicate<ClassNode> classNodeTest) {
        this.classNodeTest = classNodeTest;
        return this;
    }

    /**
     * A conditional check that will test the name of the jar files. !!! This mod identification method should only be
     * used as a last resort if you cannot identify your targeted mod with the other methods !!!
     */
    public TargetModBuilder setJarNameTest(Predicate<String> jarNameTest) {
        this.jarNameTest = jarNameTest;
        return this;
    }

    protected String getCoreModClass() {
        return coreModClass;
    }

    protected String getModId() {
        return modId;
    }

    protected String getTargetClass() {
        return targetClass;
    }

    protected Predicate<ClassNode> getClassNodeTest() {
        return classNodeTest;
    }

    protected Predicate<String> getJarNameTest() {
        return jarNameTest;
    }
}
