package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Phase;
import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Side;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("ForLoopReplaceableByForEach")
public abstract class AbstractBuilder {

    protected @Nullable List<String> commonClasses;
    protected @Nullable List<String> clientClasses;
    protected @Nullable List<String> serverClasses;
    protected @Nullable List<ITargetMod> requiredMods;
    protected @Nullable List<ITargetMod> excludedMods;
    protected @Nullable Phase phase;
    protected @Nonnull Supplier<Boolean> applyIf = () -> true;

    protected AbstractBuilder addCommonClasses(@Nonnull String... classes) {
        Objects.requireNonNull(classes);
        if (commonClasses == null) commonClasses = new ArrayList<>(4);
        Collections.addAll(commonClasses, classes);
        return this;
    }

    protected AbstractBuilder addClientClasses(@Nonnull String... classes) {
        Objects.requireNonNull(classes);
        if (clientClasses == null) clientClasses = new ArrayList<>(4);
        Collections.addAll(clientClasses, classes);
        return this;
    }

    protected AbstractBuilder addServerClasses(@Nonnull String... classes) {
        Objects.requireNonNull(classes);
        if (serverClasses == null) serverClasses = new ArrayList<>(4);
        Collections.addAll(serverClasses, classes);
        return this;
    }

    protected AbstractBuilder addSidedClasses(@Nonnull Side side, @Nonnull String... classes) {
        Objects.requireNonNull(side);
        switch (side) {
            case COMMON:
                return addCommonClasses(classes);
            case CLIENT:
                return addClientClasses(classes);
            case SERVER:
                return addServerClasses(classes);
            default:
                throw new IllegalArgumentException();
        }
    }

    protected AbstractBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
        Objects.requireNonNull(applyIf);
        this.applyIf = applyIf;
        return this;
    }

    protected AbstractBuilder addRequiredMod(@Nonnull ITargetMod mod) {
        Objects.requireNonNull(mod);
        if (requiredMods == null) requiredMods = new ArrayList<>(2);
        requiredMods.add(mod);
        return this;
    }

    protected AbstractBuilder addExcludedMod(@Nonnull ITargetMod mod) {
        Objects.requireNonNull(mod);
        if (excludedMods == null) excludedMods = new ArrayList<>(2);
        excludedMods.add(mod);
        return this;
    }

    protected void addClassesForCurrentSide(List<String> toLoad, List<String> toNotLoad) {
        final boolean isClient = FMLLaunchHandler.side().isClient();
        if (commonClasses != null) toLoad.addAll(commonClasses);
        if (clientClasses != null) {
            if (isClient) toLoad.addAll(clientClasses);
            else toNotLoad.addAll(clientClasses);
        }
        if (serverClasses != null) {
            if (!isClient) toLoad.addAll(serverClasses);
            else toNotLoad.addAll(serverClasses);
        }
    }

    protected void addAllClassesTo(List<String> list) {
        if (commonClasses != null) list.addAll(commonClasses);
        if (clientClasses != null) list.addAll(clientClasses);
        if (serverClasses != null) list.addAll(serverClasses);
    }

    protected void addAllTargetsTo(Set<ITargetMod> set) {
        if (requiredMods != null) set.addAll(requiredMods);
        if (excludedMods != null) set.addAll(excludedMods);
    }

    protected boolean shouldLoad(Set<ITargetMod> loadedTargets) {
        return allRequiredModsPresent(loadedTargets) && noExcludedModsPresent(loadedTargets);
    }

    protected boolean allRequiredModsPresent(Set<ITargetMod> loadedTargets) {
        if (requiredMods == null) return true;
        for (int i = 0; i < requiredMods.size(); i++) {
            if (!loadedTargets.contains(requiredMods.get(i))) return false;
        }
        return true;
    }

    protected boolean noExcludedModsPresent(Set<ITargetMod> loadedTargets) {
        if (excludedMods == null) return true;
        for (int i = 0; i < excludedMods.size(); i++) {
            if (loadedTargets.contains(excludedMods.get(i))) return false;
        }
        return true;
    }

    protected static void loadClasses(List<AbstractBuilder> builders, Set<ITargetMod> loadedTargets, List<String> toLoad, List<String> toNotLoad) {
        for (int i = 0; i < builders.size(); i++) {
            AbstractBuilder builder = builders.get(i);
            if (builder.shouldLoad(loadedTargets)) {
                builder.addClassesForCurrentSide(toLoad, toNotLoad);
            } else {
                builder.addAllClassesTo(toNotLoad);
            }
        }
    }

    protected static Set<ITargetMod> getLoadedTargetedMods(List<AbstractBuilder> builders, Phase loadingPhase, Set<String> loadedCoreMods, Set<String> loadedMods) {
        Set<ITargetMod> targets = new HashSet<>();
        for (int i = 0; i < builders.size(); i++) {
            builders.get(i).addAllTargetsTo(targets);
        }
        Iterator<ITargetMod> iterator = targets.iterator();
        List<ITargetMod> notPresent = new ArrayList<>();
        while (iterator.hasNext()) {
            ITargetMod target = iterator.next();
            TargetModBuilder builder = target.getBuilder();
            TargetModBuilder.validateBuilder(builder, target, loadingPhase);
            if (!builder.isTargetPresent(loadedCoreMods, loadedMods)) {
                notPresent.add(target);
                iterator.remove();
            }
        }
        if (loadingPhase == null) {
            GTNHMixins.log("ITargetMods found: {}", targets.toString());
            GTNHMixins.log("ITargetMods not found: {}", notPresent.toString());
        } else {
            GTNHMixins.log("ITargetMods found during Phase {}: {}", loadingPhase, targets.toString());
            GTNHMixins.log("ITargetMods not found during Phase {}: {}", loadingPhase, notPresent.toString());
        }
        return targets;
    }
}


