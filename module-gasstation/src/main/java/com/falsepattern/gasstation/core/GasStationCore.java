package com.falsepattern.gasstation.core;

import com.google.common.util.concurrent.Runnables;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import com.falsepattern.gasstation.Tags;
import com.falsepattern.gasstation.IEarlyMixinLoader;
import io.github.legacymoddingmc.unimixins.gasstation.GasStationModule;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 5)
@IFMLLoadingPlugin.Name(GasStationCore.PLUGIN_NAME)
@IFMLLoadingPlugin.TransformerExclusions("com.falsepattern.gasstation.core")
public class GasStationCore implements IFMLLoadingPlugin {
    public static final String PLUGIN_NAME = Tags.MODNAME + " Core Plugin";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_NAME);

    static {
        LOGGER.info("Initializing " + Tags.MODNAME + "Core");
        GasStationModule.init();
    }
    
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Object coremodList = data.get("coremodList");
        if (coremodList instanceof List) {
            // noinspection rawtypes
            for (Object coremod : (List)coremodList) {
                try {
                    Field field = coremod.getClass().getField("coreModInstance");
                    field.setAccessible(true);
                    Object theMod = field.get(coremod);
                    if (theMod instanceof IEarlyMixinLoader) {
                        IEarlyMixinLoader loader = (IEarlyMixinLoader)theMod;
                        for (String mixinConfig : loader.getMixinConfigs()) {
                            if (loader.shouldMixinConfigQueue(mixinConfig)) {
                                LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                                Mixins.addConfiguration(mixinConfig);
                                loader.onMixinConfigQueued(mixinConfig);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Unexpected error", e);
                }
            }
        }

        ((Runnable) Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing())).run();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

