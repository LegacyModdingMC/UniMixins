package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Side;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class TransformerBuilder extends AbstractBuilder {

    public TransformerBuilder() {}

    public TransformerBuilder(String description) {}

    /**
     * Add transformer classes that should be loaded both on the client and server
     */
    public TransformerBuilder addCommonTransformers(@Nonnull String... transformers) {
        return (TransformerBuilder) super.addCommonClasses(transformers);
    }

    /**
     * Add transformer classes that should only be loaded on the client
     */
    public TransformerBuilder addClientTransformers(@Nonnull String... transformers) {
        return (TransformerBuilder) super.addClientClasses(transformers);
    }

    /**
     * Add transformer classes that should only be loaded on the dedicated server
     */
    public TransformerBuilder addServerTransformers(@Nonnull String... transformers) {
        return (TransformerBuilder) super.addServerClasses(transformers);
    }

    /**
     * Add transformer classes that should be loaded on the specified side
     */
    public TransformerBuilder addSidedTransformers(@Nonnull Side side, @Nonnull String... transformers) {
        return (TransformerBuilder) super.addSidedClasses(side, transformers);
    }

    /**
     * Specify a condition that needs to be true for this transformer to load, such as checking for a config boolean.
     */
    @Override
    public TransformerBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
        return (TransformerBuilder) super.setApplyIf(applyIf);
    }

    /**
     * Specify mods that are required to be present for this transformer to load
     */
    public TransformerBuilder addRequiredMod(@Nonnull ITargetMod mod) {
        return (TransformerBuilder) super.addRequiredMod(mod);
    }

    /**
     * Specify mods that will disable this transformer if they are present
     */
    public TransformerBuilder addExcludedMod(@Nonnull ITargetMod mod) {
        return (TransformerBuilder) super.addExcludedMod(mod);
    }

    protected static <E extends Enum<E> & ITransformers> void loadTransformers(Class<E> transformerEnum, List<String> toLoad, List<String> toNotLoad) {
        List<AbstractBuilder> builders = getEnabledBuildersForPhase(transformerEnum, toNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
        loadClasses(builders, loadedTargets, toLoad, toNotLoad);
    }

    private static <E extends Enum<E> & ITransformers> List<AbstractBuilder> getEnabledBuildersForPhase(Class<E> transformerEnum, List<String> toNotLoad) {
        final E[] constants = transformerEnum.getEnumConstants();
        List<AbstractBuilder> list = new ArrayList<>(constants.length + 1);
        for (E transformer : constants) {
            TransformerBuilder builder = transformer.getBuilder();
            validateBuilder(builder, transformer);
            if (builder.applyIf.get()) {
                list.add(builder);
            } else {
                builder.addAllClassesTo(toNotLoad);
            }
        }
        return list;
    }

    private static void validateBuilder(TransformerBuilder builder, Enum<?> transformer) {
        if (builder == null) {
            throw new NullPointerException("Builder is null for ITransformer : " + transformer.name());
        }
        int count = 0;
        if (builder.commonClasses != null) count += builder.commonClasses.size();
        if (builder.clientClasses != null) count += builder.clientClasses.size();
        if (builder.serverClasses != null) count += builder.serverClasses.size();
        if (count == 0) {
            throw new IllegalArgumentException("No transformer class registered for ITransformer : " + transformer.name());
        }
    }
}
