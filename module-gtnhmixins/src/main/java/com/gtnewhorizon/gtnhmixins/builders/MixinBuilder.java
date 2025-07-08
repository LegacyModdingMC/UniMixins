package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.builders.IMixins.Phase;
import com.gtnewhorizon.gtnhmixins.builders.IMixins.Side;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public class MixinBuilder {

    private @Nullable List<String> commonMixins;
    private @Nullable List<String> clientMixins;
    private @Nullable List<String> serverMixins;
    private @Nullable List<ITargetMod> requiredMods;
    private @Nullable List<ITargetMod> excludedMods;
    private @Nullable Phase phase;
    private @Nonnull Supplier<Boolean> applyIf = () -> true;

    public MixinBuilder() {}

    public MixinBuilder(String description) {}

    /**
     * Add mixin classes that should be loaded both on the client and server
     */
    public MixinBuilder addCommonMixins(@Nonnull String... mixins) {
        Objects.requireNonNull(mixins);
        if (commonMixins == null) commonMixins = new ArrayList<>(4);
        Collections.addAll(commonMixins, mixins);
        return this;
    }

    /**
     * Add mixin classes that should only be loaded on the client
     */
    public MixinBuilder addClientMixins(@Nonnull String... mixins) {
        Objects.requireNonNull(mixins);
        if (clientMixins == null) clientMixins = new ArrayList<>(4);
        Collections.addAll(clientMixins, mixins);
        return this;
    }

    /**
     * Add mixin classes that should only be loaded on the dedicated server
     */
    public MixinBuilder addServerMixins(@Nonnull String... mixins) {
        Objects.requireNonNull(mixins);
        if (serverMixins == null) serverMixins = new ArrayList<>(4);
        Collections.addAll(serverMixins, mixins);
        return this;
    }

    /**
     * Add mixins classes that should be loaded on the specified side
     */
    public MixinBuilder addSidedMixins(@Nonnull Side side, @Nonnull String... mixins) {
        Objects.requireNonNull(side);
        switch (side) {
            case COMMON:
                return addCommonMixins(mixins);
            case CLIENT:
                return addClientMixins(mixins);
            case SERVER:
                return addServerMixins(mixins);
            default:
                throw new IllegalArgumentException();
        }
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

    /**
     * Specify a condition that needs to be true for this mixin to load, such as checking for a config boolean.
     */
    public MixinBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
        Objects.requireNonNull(applyIf);
        this.applyIf = applyIf;
        return this;
    }

    /**
     * Specify mods that are required to be present for this mixin to load
     */
    public MixinBuilder addRequiredMod(@Nonnull ITargetMod mod) {
        Objects.requireNonNull(mod);
        if (requiredMods == null) requiredMods = new ArrayList<>(2);
        requiredMods.add(mod);
        return this;
    }

    /**
     * Specify mods that will disable this mixin if they are present
     */
    public MixinBuilder addExcludedMod(@Nonnull ITargetMod mod) {
        Objects.requireNonNull(mod);
        if (excludedMods == null) excludedMods = new ArrayList<>(2);
        excludedMods.add(mod);
        return this;
    }

    private void addMixinsForCurrentSide(List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        final boolean isClient = FMLLaunchHandler.side().isClient();
        if (commonMixins != null) mixinsToLoad.addAll(commonMixins);
        if (clientMixins != null) {
            if (isClient) mixinsToLoad.addAll(clientMixins);
            else mixinsToNotLoad.addAll(clientMixins);
        }
        if (serverMixins != null) {
            if (!isClient) mixinsToLoad.addAll(serverMixins);
            else mixinsToNotLoad.addAll(serverMixins);
        }
    }

    private void addAllMixinsTo(List<String> list) {
        if (commonMixins != null) list.addAll(commonMixins);
        if (clientMixins != null) list.addAll(clientMixins);
        if (serverMixins != null) list.addAll(serverMixins);
    }

    private void addAllTargetsTo(Set<ITargetMod> set) {
        if (requiredMods != null) set.addAll(requiredMods);
        if (excludedMods != null) set.addAll(excludedMods);
    }

    private static void validateBuilder(MixinBuilder builder, Enum<?> mixin, boolean requirePhase) {
        if (builder == null) {
            throw new NullPointerException("Builder is null for IMixins : " + mixin.name());
        }
        int count = 0;
        if (builder.commonMixins != null) count += builder.commonMixins.size();
        if (builder.clientMixins != null) count += builder.clientMixins.size();
        if (builder.serverMixins != null) count += builder.serverMixins.size();
        if (count == 0) {
            throw new IllegalArgumentException("No mixin class registered for IMixins : " + mixin.name());
        }
        if (requirePhase && builder.phase == null) {
            throw new IllegalArgumentException("No Phase specified for IMixins : " + mixin.name());
        }
    }

    private boolean shouldLoadMixin(Set<ITargetMod> loadedTargets) {
        return allRequiredModsPresent(loadedTargets) && noExcludedModsPresent(loadedTargets);
    }

    private boolean allRequiredModsPresent(Set<ITargetMod> loadedTargets) {
        if (requiredMods == null) return true;
        for (int i = 0; i < requiredMods.size(); i++) {
            if (!loadedTargets.contains(requiredMods.get(i))) return false;
        }
        return true;
    }

    private boolean noExcludedModsPresent(Set<ITargetMod> loadedTargets) {
        if (excludedMods == null) return true;
        for (int i = 0; i < excludedMods.size(); i++) {
            if (loadedTargets.contains(excludedMods.get(i))) return false;
        }
        return true;
    }

    private static void validateTargetModBuilder(TargetModBuilder builder, ITargetMod target, Phase phaseIn) {
        if (builder == null) {
            throw new NullPointerException("TargetModBuilder is null");
        }
        if (builder.getModId() == null && builder.getCoreModClass() == null && builder.getTargetClass() == null && builder.getClassNodeTest() == null && builder.getJarNameTest() == null) {
            throw new IllegalArgumentException("No information at all provided by ITargetMod " + target);
        }
        if (phaseIn == Phase.EARLY) {
            if (builder.getCoreModClass() == null && builder.getTargetClass() == null && builder.getClassNodeTest() == null && builder.getJarNameTest() == null) {
                throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by early mixins");
            }
        } else if (phaseIn == Phase.LATE) {
            if (builder.getModId() == null && builder.getTargetClass() == null && builder.getClassNodeTest() == null && builder.getJarNameTest() == null) {
                throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by late mixins");
            }
        }
        if (builder.getClassNodeTest() != null && builder.getTargetClass() == null) {
            throw new IllegalArgumentException("ITargetMod " + target + " uses a ClassNode test but doesn't specify the target class");
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
            if (builder.shouldLoadMixin(loadedTargets)) {
                builder.addMixinsForCurrentSide(mixinsToLoad, mixinsToNotLoad);
            } else {
                builder.addAllMixinsTo(mixinsToNotLoad);
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
                builder.addAllMixinsTo(mixinsToNotLoad);
            }
        }
        return list;
    }

    private static Set<ITargetMod> getLoadedTargetedMods(List<MixinBuilder> builders, Phase phase, Set<String> loadedCoreMods, Set<String> loadedMods) {
        Set<ITargetMod> targets = new HashSet<>();
        for (int i = 0; i < builders.size(); i++) {
            builders.get(i).addAllTargetsTo(targets);
        }
        Iterator<ITargetMod> iterator = targets.iterator();
        while (iterator.hasNext()) {
            ITargetMod target = iterator.next();
            final TargetModBuilder builder = target.getBuilder();
            validateTargetModBuilder(builder, target, phase);
            if (!isTargetPresent(builder, loadedCoreMods, loadedMods)) {
                iterator.remove();
            }
        }
        return targets;
    }

    private static boolean isTargetPresent(TargetModBuilder target, Set<String> loadedCoreMods, Set<String> loadedMods) {
        // 1. check coremod class
        if (!loadedCoreMods.isEmpty() && target.getCoreModClass() != null && loadedCoreMods.contains(target.getCoreModClass())) {
            return true;
        }
        // 2. check modID
        if (!loadedMods.isEmpty() && target.getModId() != null && loadedMods.contains(target.getModId())) {
            return true;
        }
        // 3. check class
        if (target.getTargetClass() != null) {
            try {
                ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(target.getTargetClass(), false);
                if (target.getClassNodeTest() == null) {
                    return true;
                }
                // 4. test bytecode of target class
                final boolean test = target.getClassNodeTest().test(classNode);
                if (test) return true;
            } catch (ClassNotFoundException | IOException ignored) {}
        }
        // 5 find jar files and test jar name
        if (target.getJarNameTest() != null) {
            // TODO implement jar name matching
            throw new UnsupportedOperationException("Jar name matching isn't implemented yet");
        }
        return false;
    }

}
