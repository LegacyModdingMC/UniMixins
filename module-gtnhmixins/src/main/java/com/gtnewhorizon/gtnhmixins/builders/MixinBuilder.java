package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Phase;
import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Side;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MixinBuilder extends AbstractBuilder {

    public MixinBuilder() {}

    public MixinBuilder(String description) {}

    /**
     * Add mixin classes that should be loaded both on the client and server
     */
    public MixinBuilder addCommonMixins(@Nonnull String... mixins) {
        return (MixinBuilder) super.addCommonClasses(mixins);
    }

    /**
     * Add mixin classes that should only be loaded on the client
     */
    public MixinBuilder addClientMixins(@Nonnull String... mixins) {
        return (MixinBuilder) super.addClientClasses(mixins);
    }

    /**
     * Add mixin classes that should only be loaded on the dedicated server
     */
    public MixinBuilder addServerMixins(@Nonnull String... mixins) {
        return (MixinBuilder) super.addServerClasses(mixins);
    }

    /**
     * Add mixins classes that should be loaded on the specified side
     */
    public MixinBuilder addSidedMixins(@Nonnull Side side, @Nonnull String... mixins) {
        return (MixinBuilder) super.addSidedClasses(side, mixins);
    }

    /**
     * Specify a condition that needs to be true for this mixin to load, such as checking for a config boolean.
     */
    @Override
    public MixinBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
        return (MixinBuilder) super.setApplyIf(applyIf);
    }

    /**
     * Specify mods that are required to be present for this mixin to load
     */
    @Override
    public MixinBuilder addRequiredMod(@Nonnull ITargetMod mod) {
        return (MixinBuilder) super.addRequiredMod(mod);
    }

    /**
     * Specify mods that will disable this mixin if they are present
     */
    @Override
    public MixinBuilder addExcludedMod(@Nonnull ITargetMod mod) {
        return (MixinBuilder) super.addExcludedMod(mod);
    }

    /**
     * Mixins registered from a {@link com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader} need to set this to
     * {@link com.gtnewhorizon.gtnhmixins.builders.IMixins.Phase#EARLY}.
     * <p>
     * Mixins registered from a {@link com.gtnewhorizon.gtnhmixins.ILateMixinLoader} need to set this to
     * {@link com.gtnewhorizon.gtnhmixins.builders.IMixins.Phase#LATE}.
     * <p>
     * Mixins registered from a {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin} need to leave this
     * null.
     */
    public MixinBuilder setPhase(Phase phase) {
        this.phase = phase;
        return this;
    }

    protected static <E extends Enum<E> & IMixins> void loadMixins(Class<E> mixinsEnum, List<String> toLoad, List<String> toNotLoad) {
        List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, null, toNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
        loadClasses(builders, loadedTargets, toLoad, toNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadEarlyMixins(Class<E> mixinsEnum, Set<String> loadedCoreMods, List<String> toLoad, List<String> toNotLoad) {
        List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.EARLY, toNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, Phase.EARLY, loadedCoreMods, Collections.emptySet());
        loadClasses(builders, loadedTargets, toLoad, toNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadLateMixins(Class<E> mixinsEnum, Set<String> loadedMods, List<String> toLoad, List<String> toNotLoad) {
        List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.LATE, toNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, Phase.LATE, Collections.emptySet(), loadedMods);
        loadClasses(builders, loadedTargets, toLoad, toNotLoad);
    }

    private static <E extends Enum<E> & IMixins> List<AbstractBuilder> getEnabledBuildersForPhase(Class<E> mixinsEnum, Phase loadingPhase, List<String> toNotLoad) {
        final E[] constants = mixinsEnum.getEnumConstants();
        List<AbstractBuilder> list = new ArrayList<>(constants.length + 1);
        for (E mixin : constants) {
            MixinBuilder builder = mixin.getBuilder();
            validateBuilder(builder, mixin, loadingPhase != null);
            if (builder.phase != loadingPhase) continue;
            if (builder.applyIf.get()) {
                list.add(builder);
            } else {
                builder.addAllClassesTo(toNotLoad);
            }
        }
        return list;
    }

    private static void validateBuilder(MixinBuilder builder, Enum<?> mixin, boolean requirePhase) {
        if (builder == null) {
            throw new NullPointerException("Builder is null for IMixins : " + mixin.name());
        }
        int count = 0;
        if (builder.commonClasses != null) count += builder.commonClasses.size();
        if (builder.clientClasses != null) count += builder.clientClasses.size();
        if (builder.serverClasses != null) count += builder.serverClasses.size();
        if (count == 0) {
            throw new IllegalArgumentException("No mixin class registered for IMixins : " + mixin.name());
        }
        if (requirePhase && builder.phase == null) {
            throw new IllegalArgumentException("No Phase specified for IMixins : " + mixin.name());
        }
    }
}
