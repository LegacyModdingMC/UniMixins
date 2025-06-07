package io.github.legacymoddingmc.unimixins.mixin;

import cpw.mods.fml.common.Mod;

/**
 * An empty mod class provided to make it impossible to load multiple editions
 * of the module at the same time.
 */
@Mod(modid = "unimixins-mixin", version = "@VERSION@", acceptableRemoteVersions = "*")
@net.minecraftforge.fml.common.Mod(modid = "unimixins-mixin", version = "@VERSION@", acceptableRemoteVersions = "*")
public class MixinModule {}
