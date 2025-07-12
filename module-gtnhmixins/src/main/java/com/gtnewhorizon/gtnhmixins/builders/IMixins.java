package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The IMixins interface provides a flexible way of declaring and registering mixins during runtime.
 * Registration is done based on conditions such as user defined logic, looking configs, loading side (CLIENT, COMMON, SERVER)
 * or looking at the presence or absence of mods declared using the {@link com.gtnewhorizon.gtnhmixins.builders.ITargetMod} interface.
 * This interface must be implemented on an enum.
 * <pre>
 * {@code
 * public enum Mixins implements IMixins {
 *
 *     EXAMPLE_MIXIN(new MixinBuilder("Hello") // optional comment
 *         .addRequiredMod(TargetMod.IC2) // optional
 *         .addExcludedMod(...) // optional
 *         .setPhase(Phase.EARLY) // optional, required for gtnh mixins
 *         .addCommonMixins("........") // mixins that needs to be both on client and server
 *         .addClientMixins("........") // mixins only for the client
 *         .addServerMixins("........") // mixins only for the dedicated server
 *         .setApplyIf(() -> Config.yourconfig....)); // optional
 *         // it is required to have at least one mixin class
 *
 *     private final MixinBuilder builder;
 *
 *     Mixins(MixinBuilder builder) {
 *         this.builder = builder;
 *     }
 *
 *     @Override
 *     public MixinBuilder getBuilder() {
 *         return this.builder;
 *     }
 * }
 * }
 * </pre>
 * <p>
 * If you do not need very complex logic in your enum, you can instantiate
 * the MixinBuilder in the enum constructor and declare simplistic enum entries.
 *
 * <pre>
 * {@code
 * public enum Mixins implements IMixins {
 *
 *     CLIENT_MIXIN1(Side.CLIENT, "MixinClientClass1"),
 *     CLIENT_MIXIN2(Side.CLIENT, "MixinClientClass2"),
 *     COMMON_MIXIN(Side.COMMON, "MixinCommonClass");
 *
 *     private final MixinBuilder builder;
 *
 *     Mixins(Side side, String... mixins) {
 *         builder = new MixinBuilder().addSidedMixins(side, mixins);
 *     }
 *
 *     @Override
 *     public MixinBuilder getBuilder() {
 *         return this.builder;
 *     }
 * }
 * }
 * </pre>
 */
@SuppressWarnings("unused")
public interface IMixins extends IBaseTransformer {

    @Nonnull
    MixinBuilder getBuilder();

    /**
     * Returns the list of mixins that should be loaded by your {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin} implementation.
     * <p>
     * You may call it as such :
     * <pre>
     * {@code
     *     @Override
     *     public List<String> getMixins() {
     *         return IMixins.getMixins(YourMixinEnum.class);
     *     }
     * }
     * </pre>
     */
    static <E extends Enum<E> & IMixins> List<String> getMixins(Class<E> mixinsEnum) {
        final List<String> toLoad = new ArrayList<>();
        final List<String> toNotLoad = new ArrayList<>();
        MixinBuilder.loadMixins(mixinsEnum, toLoad, toNotLoad);
        GTNHMixins.log("Not loading the following mixins: {}", toNotLoad);
        for (String mixin : toLoad) {
            GTNHMixins.log("Loading {}", mixin);
        }
        return toLoad;
    }

    /**
     * Returns the list of mixins that should be loaded by your {@link com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader} implementation.
     * Note that you also need to implement {@link cpw.mods.fml.relauncher.IFMLLoadingPlugin} for early mixins to work.
     * <p>
     * You may call it as such :
     * <pre>
     * {@code
     *     @Override
     *     public List<String> getMixins(Set<String> loadedCoreMods) {
     *         return IMixins.getEarlyMixins(YourMixinEnum.class, loadedCoreMods);
     *     }
     * }
     * </pre>
     */
    static <E extends Enum<E> & IMixins> List<String> getEarlyMixins(Class<E> mixinsEnum, Set<String> loadedCoreMods) {
        final List<String> toLoad = new ArrayList<>();
        final List<String> toNotLoad = new ArrayList<>();
        MixinBuilder.loadEarlyMixins(mixinsEnum, loadedCoreMods, toLoad, toNotLoad);
        GTNHMixins.log("Not loading the following EARLY mixins: {}", toNotLoad);
        return toLoad;
    }

    /**
     * Returns the list of mixins that should be loaded by your {@link com.gtnewhorizon.gtnhmixins.ILateMixinLoader} implementation.
     * Note that you also need to annotate your class with {@link com.gtnewhorizon.gtnhmixins.LateMixin} for late mixins to work.
     * <p>
     * You may call it as such :
     * <pre>
     * {@code
     *     @Override
     *     public List<String> getMixins(Set<String> loadedMods) {
     *         return IMixins.getLateMixins(YourMixinEnum.class, loadedMods);
     *     }
     * }
     * </pre>
     */
    static <E extends Enum<E> & IMixins> List<String> getLateMixins(Class<E> mixinsEnum, Set<String> loadedMods) {
        final List<String> toLoad = new ArrayList<>();
        final List<String> toNotLoad = new ArrayList<>();
        MixinBuilder.loadLateMixins(mixinsEnum, loadedMods, toLoad, toNotLoad);
        GTNHMixins.log("Not loading the following LATE mixins: {}", toNotLoad.toString());
        return toLoad;
    }
}
