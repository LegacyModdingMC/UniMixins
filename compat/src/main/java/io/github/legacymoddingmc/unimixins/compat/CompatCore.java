package io.github.legacymoddingmc.unimixins.compat;

import java.util.*;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.common.config.ConfigUtil;
import io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

@MCVersion("1.7.10")
public class CompatCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    public CompatCore() {
        LOGGER.info("Instantiating CompatCore");

        ConfigUtil.load(CompatConfig.class);

        if(CompatConfig.enableRemapper) {
            // We register the transformer this way to register it as early as possible.
            Launch.classLoader.registerTransformer(relativeClassName("asm.ASMRemapperTransformer"));
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        List<String> classes = new ArrayList<>();
        if(CompatConfig.enhanceCrashReports) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.EnhanceCrashReportsTransformer");
        }
        if(IgnoreDuplicateJarsTransformer.wantsToRun()) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer");
        }
        if(Boolean.parseBoolean(System.getProperty("unimixins.compat.hackClasspathModDiscovery", "false"))) {
            classes.add("io.github.legacymoddingmc.unimixins.compat.asm.HackClasspathModDiscoveryTransformer");
        }
        return classes.toArray(new String[0]);
    }

    private static String relativeClassName(String relName) {
        String name = CompatCore.class.getName();
        name = name.substring(0, name.lastIndexOf('.') + 1);
        name += relName;
        return name;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    /**
     * <p>This is the earliest point after the Mixin environment's initialization where we can inject code.
     * <p>We register our error handler here.
     */
    @Override
    public void injectData(Map<String, Object> data) {
        if(CompatConfig.enhanceCrashReports) {
            Mixins.registerErrorHandlerClass("io.github.legacymoddingmc.unimixins.compat.MixinErrorHandler");
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return "io.github.legacymoddingmc.unimixins.compat.CompatCore$DummyTransformer";
    }

    /** Coremod Access Transformers are initialized at the beginning of {@link cpw.mods.fml.common.launcher.FMLDeobfTweaker#injectIntoClassLoader}.
     * We use this as an injection point, this transformer doesn't actually transform anything. */
    @SuppressWarnings("unused")
    public static class DummyTransformer implements IClassTransformer {
        public DummyTransformer() {
            if (CompatConfig.improveInitPhaseDetection) {
                setFmlLoggerToVerbose();
            }
        }

        /** Mixin detects the start of the INIT phase by listening for the "Validating minecraft" FML log message.
         * (See <tt>MixinAppender</tt> in {@link org.spongepowered.asm.launch.platform.MixinPlatformAgentFMLLegacy}.)
         *
         * <p>It sets the verbosity to ALL in an attempt to ensure the message gets logged, but this is not always the
         * case. We add an extra check here, in a method that gets called right before that log message.</p>
         */
        private static void setFmlLoggerToVerbose() {
            Logger fmlLog;
            try {
                fmlLog = FMLRelaunchLog.log.getLogger();
                if (!(fmlLog instanceof org.apache.logging.log4j.core.Logger)) {
                    return;
                }
            } catch (NoClassDefFoundError e) {
                return;
            }

            org.apache.logging.log4j.core.Logger fmlCoreLog = (org.apache.logging.log4j.core.Logger)fmlLog;

            if(fmlCoreLog.getLevel() != Level.ALL) {
                LOGGER.info("Correcting FML log level from " + fmlCoreLog.getLevel() + " to ALL");
            } else {
                LOGGER.debug("FML log level was already ALL, doing nothing");
            }

            fmlCoreLog.setLevel(Level.ALL);
        }

        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            return basicClass;
        }
    }

}
