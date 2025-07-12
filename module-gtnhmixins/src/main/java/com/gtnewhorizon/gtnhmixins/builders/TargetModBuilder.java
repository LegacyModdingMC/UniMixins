package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Phase;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class TargetModBuilder implements ITargetMod {

    private String coreModClass;
    private String modId;
    private String targetClass;
    private Predicate<ClassNode> classNodeTest;

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
     * {@link TargetModBuilder#setTargetClass}. Requires you to specify a class to target via {@link TargetModBuilder#setTargetClass}.
     */
    public TargetModBuilder setClassNodeTest(Predicate<ClassNode> classNodeTest) {
        this.classNodeTest = classNodeTest;
        return this;
    }

    /**
     * A conditional check that will test the contents of the @Mod annotation of the class specified in
     * {@link TargetModBuilder#setTargetClass}. Requires you to specify a class to target via {@link TargetModBuilder#setTargetClass}.
     * Tests if the modId of the target class is equals to the modId provided.
     */
    public TargetModBuilder testModID(String modId) {
        this.classNodeTest = Predicates.testModAnnotation(Predicates.equals(modId), null, null);
        return this;
    }

    /**
     * A conditional check that will test the contents of the @Mod annotation of the class specified in
     * {@link TargetModBuilder#setTargetClass}. Requires you to specify a class to target via {@link TargetModBuilder#setTargetClass}.
     * Tests if the modId and modVersion of the target class are equal to the modId and modVersion provided.
     */
    public TargetModBuilder testModVersion(String modId, String modVersion) {
        this.classNodeTest = Predicates.testModAnnotation(Predicates.equals(modId), null, Predicates.equals(modVersion));
        return this;
    }

    /**
     * A conditional check that will test the contents of the @Mod annotation of the class specified in
     * {@link TargetModBuilder#setTargetClass}. Requires you to specify a class to target via {@link TargetModBuilder#setTargetClass}.
     * Tests if the modId of the target class is equals to the modId provided and tests the modVersion with the provided test.
     */
    public TargetModBuilder testModVersion(String modId, Predicate<String> modVersionTest) {
        this.classNodeTest = Predicates.testModAnnotation(Predicates.equals(modId), null, modVersionTest);
        return this;
    }

    /**
     * A conditional check that will test the contents of the @Mod annotation of the class specified in
     * {@link TargetModBuilder#setTargetClass}. Requires you to specify a class to target via {@link TargetModBuilder#setTargetClass}.
     * Some arguments can be null but not all at once.
     */
    public TargetModBuilder testModAnnotation(Predicate<String> modIdTest, Predicate<String> modNameTest, Predicate<String> modVersionTest) {
        this.classNodeTest = Predicates.testModAnnotation(modIdTest, modNameTest, modVersionTest);
        return this;
    }

    protected static void validateBuilder(TargetModBuilder builder, ITargetMod target, Phase phaseIn) {
        if (builder == null) {
            throw new NullPointerException("TargetModBuilder is null for ITargetMod " + target);
        }
        if (builder.modId == null && builder.coreModClass == null && builder.targetClass == null && builder.classNodeTest == null) {
            throw new IllegalArgumentException("No information at all provided by ITargetMod " + target);
        }
        if (phaseIn == Phase.EARLY) {
            if (builder.coreModClass == null && builder.targetClass == null && builder.classNodeTest == null) {
                throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by early mixins");
            }
        } else if (phaseIn == Phase.LATE) {
            if (builder.modId == null && builder.targetClass == null && builder.classNodeTest == null) {
                throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by late mixins");
            }
        } else {
            if (builder.targetClass == null && builder.classNodeTest == null) {
                throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target);
            }
        }
        if (builder.classNodeTest != null && builder.targetClass == null) {
            throw new IllegalArgumentException("ITargetMod " + target + " uses a ClassNode test but doesn't specify the target class");
        }
    }

    private boolean foundTargetClass;

    protected boolean isTargetPresent(Set<String> loadedCoreMods, Set<String> loadedMods) {
        // 1. check coremod class (early mixins only)
        if (!loadedCoreMods.isEmpty() && this.coreModClass != null && loadedCoreMods.contains(this.coreModClass)) {
            return true;
        }
        // 2. check modID (late mixins only)
        if (!loadedMods.isEmpty() && this.modId != null && loadedMods.contains(this.modId)) {
            return true;
        }
        // 3. check class
        if (this.targetClass != null) {
            if (foundTargetClass) {
                return true;
            }
            try {
                ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(this.targetClass, false);
                if (this.classNodeTest == null) {
                    foundTargetClass = true;
                    return true;
                }
                // 4. test bytecode of this class
                final boolean test = this.classNodeTest.test(classNode);
                if (test) {
                    foundTargetClass = true;
                    return true;
                }
            } catch (ClassNotFoundException | IOException ignored) {}
        }
        return false;
    }

    @Nonnull
    @Override
    public TargetModBuilder getBuilder() {
        return this;
    }
}
