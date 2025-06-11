package com.gtnewhorizon.gtnhmixins;

import java.util.List;
import java.util.Set;

/**
 * Early mixins are defined as mixins that affects vanilla or forge classes.
 * Or technically, classes that can be queried via the current state of {@link net.minecraft.launchwrapper.LaunchClassLoader}
 * <p>
 * If you want to add mixins that affect mods, use {@link ILateMixinLoader}
 * <p>
 * Implement this in your {@link cpw.mods.fml.relauncher.IFMLLoadingPlugin}.
 * Return all early mixin configs you want MixinBooter to queue and send to Mixin library.
 */
public interface IEarlyMixinLoader{

    /**
     * @return the mixin config, generally `mixins.[modid].early.json`
     */
    String getMixinConfig();
    
    /**
     * @param loadedCoreMods Set of loaded core mods, for use in discrimination of what mixins load
     * @return mixin configurations to be queued and sent to Mixin library.
     */
    List<String> getMixins(Set<String> loadedCoreMods);
}