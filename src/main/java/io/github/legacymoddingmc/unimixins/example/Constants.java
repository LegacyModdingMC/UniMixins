package io.github.legacymoddingmc.unimixins.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// In a separate class so we can safely reference it in coremod code without triggering the mod class to load early
public class Constants {
    public static final String MODID = "unimixinsexample";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
}
