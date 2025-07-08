package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.builders.IMixins.Phase;
import com.gtnewhorizon.gtnhmixins.builders.IMixins.Side;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
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

    protected static <E extends Enum<E> & IMixins> void loadMixins(Class<E> mixinsEnum, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, null, mixinsToNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadEarlyMixins(Class<E> mixinsEnum, Set<String> loadedCoreMods, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.EARLY, mixinsToNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, Phase.EARLY, loadedCoreMods, Collections.emptySet());
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadLateMixins(Class<E> mixinsEnum, Set<String> loadedMods, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.LATE, mixinsToNotLoad);
        Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, Phase.LATE, Collections.emptySet(), loadedMods);
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    private static void loadMixins(List<MixinBuilder> builders, Set<ITargetMod> loadedTargets, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        for (int i = 0; i < builders.size(); i++) {
            MixinBuilder builder = builders.get(i);
            if (builder.shouldLoad(loadedTargets)) {
                builder.addClassesForCurrentSide(mixinsToLoad, mixinsToNotLoad);
            } else {
                builder.addAllClassesTo(mixinsToNotLoad);
            }
        }
    }

    private static <E extends Enum<E> & IMixins> List<MixinBuilder> getEnabledBuildersForPhase(Class<E> mixinsEnum, Phase loadingPhase, List<String> mixinsToNotLoad) {
        final E[] constants = mixinsEnum.getEnumConstants();
        List<MixinBuilder> list = new ArrayList<>(constants.length + 1);
        for (E mixin : constants) {
            MixinBuilder builder = mixin.getBuilder();
            validateBuilder(builder, mixin, loadingPhase != null);
            if (builder.phase != loadingPhase) continue;
            if (builder.applyIf.get()) {
                list.add(builder);
            } else {
                builder.addAllClassesTo(mixinsToNotLoad);
            }
        }
        return list;
    }

    private static Set<ITargetMod> getLoadedTargetedMods(List<MixinBuilder> builders, Phase loadingPhase, Set<String> loadedCoreMods, Set<String> loadedMods) {
        Set<ITargetMod> targets = new HashSet<>();
        for (int i = 0; i < builders.size(); i++) {
            builders.get(i).addAllTargetsTo(targets);
        }
        Iterator<ITargetMod> iterator = targets.iterator();
        while (iterator.hasNext()) {
            ITargetMod target = iterator.next();
            TargetModBuilder builder = target.getBuilder();
            TargetModBuilder.validateBuilder(builder, target, loadingPhase);
            if (!builder.isTargetPresent(loadedCoreMods, loadedMods)) {
                iterator.remove();
            }
        }
        return targets;
    }
}
