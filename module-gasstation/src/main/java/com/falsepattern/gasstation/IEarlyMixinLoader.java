package com.falsepattern.gasstation;

import java.util.List;

/**
 * Use {@link com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader} instead!
 */
@Deprecated
public interface IEarlyMixinLoader {

    /**
     * @return mixin configurations to be queued and sent to Mixin library.
     */
    List<String> getMixinConfigs();

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     *
     * @param mixinConfig mixin config name, queried via {@link IEarlyMixinLoader#getMixinConfigs()}.
     * @return true if the mixinConfig should be queued, false if it should not.
     */
    default boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     *
     * @param mixinConfig mixin config name, queried via {@link IEarlyMixinLoader#getMixinConfigs()}.
     */
    default void onMixinConfigQueued(String mixinConfig) {
    }
}