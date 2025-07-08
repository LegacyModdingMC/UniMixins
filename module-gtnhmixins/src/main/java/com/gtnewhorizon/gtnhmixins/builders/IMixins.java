package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This interface needs to be implemented on an enum that declares all your mixins
 */
@SuppressWarnings("unused")
public interface IMixins {

    @Nonnull
    MixinBuilder getBuilder();

    /**
     * Returns the list of mixins that should be loaded from your {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin} implementation.
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
        final List<String> mixinsToLoad = new ArrayList<>();
        final List<String> mixinsToNotLoad = new ArrayList<>();
        MixinBuilder.loadMixins(mixinsEnum, mixinsToLoad, mixinsToNotLoad);
        final Logger LOGGER = LogManager.getLogger("IMixins Loader");
        LOGGER.info("Not loading the following mixins: {}", mixinsToNotLoad);
        return mixinsToLoad;
    }

    /**
     * Returns the list of mixins that should be loaded from your {@link com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader} implementation.
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
        final List<String> mixinsToLoad = new ArrayList<>();
        final List<String> mixinsToNotLoad = new ArrayList<>();
        MixinBuilder.loadEarlyMixins(mixinsEnum, loadedCoreMods, mixinsToLoad, mixinsToNotLoad);
        GTNHMixins.LOGGER.info("Not loading the following EARLY mixins: {}", mixinsToNotLoad);
        return mixinsToLoad;
    }

    /**
     * Returns the list of mixins that should be loaded from your {@link com.gtnewhorizon.gtnhmixins.ILateMixinLoader} implementation.
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
        final List<String> mixinsToLoad = new ArrayList<>();
        final List<String> mixinsToNotLoad = new ArrayList<>();
        MixinBuilder.loadLateMixins(mixinsEnum, loadedMods, mixinsToLoad, mixinsToNotLoad);
        GTNHMixins.LOGGER.info("Not loading the following LATE mixins: {}", mixinsToNotLoad.toString());
        return mixinsToLoad;
    }

    /**
     * Phase is only used for early and late mixins from gtnh mixins
     */
    enum Phase {
        EARLY,
        LATE
    }

    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}
