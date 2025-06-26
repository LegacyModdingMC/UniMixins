package com.gtnewhorizon.gtnhmixins;

import java.util.List;
import java.util.Set;

/**
 * Late mixins are defined as mixins that affects mod classes. Or technically, classes that can be queried via the current state of
 * {@link net.minecraft.launchwrapper.LaunchClassLoader}
 * <p>
 * Majority if not all vanilla and forge classes would have been loaded here.
 * If you want to add mixins that affect vanilla or forge, use and consult {@link IEarlyMixinLoader}
 * <p>
 * Implement this in any arbitrary class, along with the annotation @{@link LateMixin}.  The annotation will cause the class to be constructed when mixins are 
 * ready to be queued. Return all of the mixin classes (under the mixinConfig's package) that you want to queue and send to Mixin library.
 */
public interface ILateMixinLoader {

    /**
     * @return the mixin config, generally `mixins.[modid].late.json`
     */
    String getMixinConfig();

    /**
     * @param loadedMods Set of loaded modids, for use in discrimination of what mixins load
     * @return mixin configurations to be queued and sent to Mixin library.
     */
    List<String> getMixins(Set<String> loadedMods);
}