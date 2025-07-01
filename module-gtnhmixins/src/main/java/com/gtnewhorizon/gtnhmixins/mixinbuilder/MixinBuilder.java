package com.gtnewhorizon.gtnhmixins.mixinbuilder;

import com.gtnewhorizon.gtnhmixins.mixinbuilder.IMixins.Phase;
import com.gtnewhorizon.gtnhmixins.mixinbuilder.IMixins.Side;
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
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public class MixinBuilder {

    private @Nullable List<String> commonMixins;
    private @Nullable List<String> clientMixins;
    private @Nullable List<String> serverMixins;
    private @Nullable List<ITargetedMod> requiredMods;
    private @Nullable List<ITargetedMod> excludedMods;
    private @Nullable Phase phase;
    private @Nonnull Supplier<Boolean> applyIf = () -> true;

    public MixinBuilder() {}

    public MixinBuilder(String description) {}

    /**
     * Add mixin classes that should be loaded both on the client and server
     */
    public MixinBuilder addCommonMixins(@Nonnull String... mixins) {
        if (commonMixins == null) commonMixins = new ArrayList<>(4);
        Collections.addAll(commonMixins, mixins);
        return this;
    }

    /**
     * Add mixin classes that should only be loaded on the client
     */
    public MixinBuilder addClientMixins(@Nonnull String... mixins) {
        if (clientMixins == null) clientMixins = new ArrayList<>(4);
        Collections.addAll(clientMixins, mixins);
        return this;
    }

    /**
     * Add mixin classes that should only be loaded on the dedicated server
     */
    public MixinBuilder addServerMixins(@Nonnull String... mixins) {
        if (serverMixins == null) serverMixins = new ArrayList<>(4);
        Collections.addAll(serverMixins, mixins);
        return this;
    }

    /**
     * Add mixins classes that should be loaded on the specified side
     */
    public MixinBuilder addSidedMixins(Side side, @Nonnull String... mixins) {
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
     * {@link com.gtnewhorizon.gtnhmixins.mixinbuilder.IMixins.Phase#EARLY}.
     * <p>
     * Mixins registered from a {@link com.gtnewhorizon.gtnhmixins.ILateMixinLoader} need to set this to
     * {@link com.gtnewhorizon.gtnhmixins.mixinbuilder.IMixins.Phase#LATE}.
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
        this.applyIf = applyIf;
        return this;
    }

    /**
     * Specify mods that are required to be present for this mixin to load
     */
    public MixinBuilder addRequiredMod(@Nonnull ITargetedMod mod) {
        if (requiredMods == null) requiredMods = new ArrayList<>(2);
        requiredMods.add(mod);
        return this;
    }

    /**
     * Specify mods that will disable this mixin if they are present
     */
    public MixinBuilder addExcludedMod(@Nonnull ITargetedMod mod) {
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

    private void addAllTargetsTo(Set<ITargetedMod> set) {
        if (requiredMods != null) set.addAll(requiredMods);
        if (excludedMods != null) set.addAll(excludedMods);
    }

    private void validateBuilder(Enum<?> mixin, boolean requirePhase) {
        int count = 0;
        if (commonMixins != null) count += commonMixins.size();
        if (clientMixins != null) count += clientMixins.size();
        if (serverMixins != null) count += serverMixins.size();
        if (count == 0) {
            throw new RuntimeException("No mixin class registered for IMixins : " + mixin.name());
        }
        if (requirePhase && phase == null) {
            throw new RuntimeException("No Phase specified for IMixins : " + mixin.name());
        }
    }

    private boolean shouldLoadMixin(Set<ITargetedMod> loadedTargets) {
        return allRequiredModsPresent(loadedTargets) && noExcludedModsPresent(loadedTargets);
    }

    private boolean allRequiredModsPresent(Set<ITargetedMod> loadedTargets) {
        if (requiredMods == null) return true;
        for (int i = 0; i < requiredMods.size(); i++) {
            if (!loadedTargets.contains(requiredMods.get(i))) return false;
        }
        return true;
    }

    private boolean noExcludedModsPresent(Set<ITargetedMod> loadedTargets) {
        if (excludedMods == null) return true;
        for (int i = 0; i < excludedMods.size(); i++) {
            if (loadedTargets.contains(excludedMods.get(i))) return false;
        }
        return true;
    }

    private static void validateTargetedMod(ITargetedMod target, Phase phaseIn) {
        if (target == null) {
            throw new NullPointerException("ITargetedMod is null!");
        }
        if (target.getModId() == null && target.getCoreModClass() == null && target.getTargetClass() == null && target.getClassNodeTest() == null && target.getJarNameTest() == null) {
            throw new RuntimeException("No information at all provided by ITargetedMod " + target);
        }
        if (phaseIn == Phase.EARLY) {
            if (target.getCoreModClass() == null && target.getTargetClass() == null && target.getClassNodeTest() == null && target.getJarNameTest() == null) {
                throw new RuntimeException("Not enough information provided by ITargetedMod " + target + " used by early mixins");
            }
        } else if (phaseIn == Phase.LATE) {
            if (target.getModId() == null && target.getTargetClass() == null && target.getClassNodeTest() == null && target.getJarNameTest() == null) {
                throw new RuntimeException("Not enough information provided by ITargetedMod " + target + " used by late mixins");
            }
        }
        if (target.getClassNodeTest() != null && target.getTargetClass() == null) {
            throw new RuntimeException("ITargetedMod " + target + " uses a ClassNode test but doesn't specify the target class");
        }
    }

    protected static <E extends Enum<E> & IMixins> void loadMixins(Class<E> mixinsEnum, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, null, mixinsToNotLoad);
        Set<ITargetedMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadEarlyMixins(Class<E> mixinsEnum, Set<String> loadedCoreMods, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.EARLY, mixinsToNotLoad);
        Set<ITargetedMod> loadedTargets = getLoadedTargetedMods(builders, Phase.EARLY, loadedCoreMods, Collections.emptySet());
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    protected static <E extends Enum<E> & IMixins> void loadLateMixins(Class<E> mixinsEnum, Set<String> loadedMods, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
        List<MixinBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, Phase.LATE, mixinsToNotLoad);
        Set<ITargetedMod> loadedTargets = getLoadedTargetedMods(builders, Phase.LATE, Collections.emptySet(), loadedMods);
        loadMixins(builders, loadedTargets, mixinsToLoad, mixinsToNotLoad);
    }

    private static void loadMixins(List<MixinBuilder> builders, Set<ITargetedMod> loadedTargets, List<String> mixinsToLoad, List<String> mixinsToNotLoad) {
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
            builder.validateBuilder(mixin, loadingPhase != null);
            if (builder.phase != loadingPhase) continue;
            if (builder.applyIf.get()) {
                list.add(builder);
            } else {
                builder.addAllMixinsTo(mixinsToNotLoad);
            }
        }
        return list;
    }

    private static Set<ITargetedMod> getLoadedTargetedMods(List<MixinBuilder> builders, Phase phase, Set<String> loadedCoreMods, Set<String> loadedMods) {
        Set<ITargetedMod> targets = new HashSet<>();
        for (int i = 0; i < builders.size(); i++) {
            builders.get(i).addAllTargetsTo(targets);
        }
        Iterator<ITargetedMod> iterator = targets.iterator();
        while (iterator.hasNext()) {
            ITargetedMod target = iterator.next();
            validateTargetedMod(target, phase);
            if (!isTargetPresent(target, loadedCoreMods, loadedMods)) {
                iterator.remove();
            }
        }
        return targets;
    }

    private static boolean isTargetPresent(ITargetedMod target, Set<String> loadedCoreMods, Set<String> loadedMods) {
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
