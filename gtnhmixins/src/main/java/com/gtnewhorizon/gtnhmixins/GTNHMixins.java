package com.gtnewhorizon.gtnhmixins;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = GTNHMixins.MODID, name = GTNHMixins.NAME, useMetadata = true, acceptableRemoteVersions = "*")
@net.minecraftforge.fml.common.Mod(modid = GTNHMixins.MODID, name = GTNHMixins.NAME, useMetadata = true, acceptableRemoteVersions = "*")
public class GTNHMixins {
    public static final String NAME = "GTNHMixins";
    public static final String MODID = "gtnhmixins";
    /**
     * @deprecated This may not actually reflect the version, use FML methods to detect the version if needed.
     */
    @Deprecated
    public static final String VERSION = "2.2.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
}