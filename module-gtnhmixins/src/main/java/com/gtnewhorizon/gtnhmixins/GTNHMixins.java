package com.gtnewhorizon.gtnhmixins;

import com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore;
import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = GTNHMixins.MODID, version = GTNHMixins.VERSION, name = GTNHMixins.NAME, acceptableRemoteVersions = "*")
@net.minecraftforge.fml.common.Mod(modid = GTNHMixins.MODID, version = GTNHMixins.VERSION, name = GTNHMixins.NAME, acceptableRemoteVersions = "*")
public class GTNHMixins {
    public static final String NAME = "GTNHMixins";
    public static final String MODID = "gtnhmixins";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    /**
     * Logs at level info in de-obfuscated environment, level debug otherwise
     */
    public static void log(String message) {
        if (GTNHMixinsCore.isObf()) {
            LOGGER.debug(message);
        } else {
            LOGGER.info(message);
        }
    }

    /**
     * Logs at level info in de-obfuscated environment, level debug otherwise
     */
    public static void log(String message, Object... params) {
        if (GTNHMixinsCore.isObf()) {
            LOGGER.debug(message, params);
        } else {
            LOGGER.info(message, params);
        }
    }
}