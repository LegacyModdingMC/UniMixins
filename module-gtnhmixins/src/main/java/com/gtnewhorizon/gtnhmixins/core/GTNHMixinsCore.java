package com.gtnewhorizon.gtnhmixins.core;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.Reflection;
import com.gtnewhorizon.gtnhmixinslite.core.GTNHMixinsLite;
import com.gtnewhorizon.mixinextras.MixinExtrasBootstrap;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.transformer.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 5)
@IFMLLoadingPlugin.Name(GTNHMixinsCore.PLUGIN_NAME)
@IFMLLoadingPlugin.TransformerExclusions("com.gtnewhorizon.gtnhmixins.core")
public class GTNHMixinsCore implements IFMLLoadingPlugin {
    public static final String PLUGIN_NAME = "GTNHMixins Core Plugin";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_NAME);

    /**
     * Core mods we need to Manually look for:
     *    (classNameToFind, coreModNameToUse)
     */
    public static final Map<String, String> MANUALLY_IDENTIFIED_COREMODS = ImmutableMap.<String, String>builder()
        .put("optifine.OptiFineForgeTweaker", "optifine.OptiFineForgeTweaker")
        .put("codechicken.lib.asm.ModularASMTransformer", "codechicken.lib")
        .put("org.bukkit.World", "Bukkit")
        .build();

    static {
        LOGGER.info("Initializing GTNHMixins Core");
        
        GTNHMixinsLite.init();
        MixinExtrasBootstrap.init();
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

    private Set<String> getLoadedCoremods(List<?> coremodList) {
        final Set<String> loadedCoremods = new HashSet<>();
        
        // Manual "Coremod" identification.  Be sure you're not loading classes too early   
        MANUALLY_IDENTIFIED_COREMODS.forEach((clsName, coreModIdentifier) -> {
            try {
                Class.forName(clsName);
                loadedCoremods.add(coreModIdentifier);
            } catch (ClassNotFoundException ignored) {}            
        });

        
        // Grab a list of tweakers (fastcraft)
        for (Object tweak : (ArrayList<?>)Launch.blackboard.get("Tweaks")) {
            final Object obj;
            try {
                obj = Reflection.pluginWrapperClass.isInstance(tweak) ? Reflection.coreModInstanceField.get(tweak) : tweak; 
                loadedCoremods.add(obj.toString().split("@")[0]);
            } catch(Exception ignored) {}
        }
        // Now coremods
        for (Object coremod : coremodList) {
            try {
                Object theMod = Reflection.coreModInstanceField.get(coremod);
                loadedCoremods.add(theMod.toString().split("@")[0]);
            } catch (Exception ignored) {}
        }
        return loadedCoremods;
    }
    
    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.info("Examining core mod list");
        final Object coremodList = data.get("coremodList");
        
        if (coremodList instanceof List) {
            final Set<String> loadedCoremods = getLoadedCoremods((List<?>) coremodList);

            LOGGER.info("LoadedCoreMods {}", loadedCoremods.toString());
            for (Object coremod : (List<?>)coremodList) {
                // Identify any coremods that are `IEarlyMixinLoader`, and inject any relevant mixins 
                try {
                    Object theMod = Reflection.coreModInstanceField.get(coremod);
                    if (theMod instanceof IEarlyMixinLoader) {
                        final IEarlyMixinLoader loader = (IEarlyMixinLoader)theMod;
                        final String mixinConfig = loader.getMixinConfig();
                        final Config config = Config.create(mixinConfig);
                        final List<String> mixins = loader.getMixins(loadedCoremods);
                        for(String mixin : mixins) {
                            LOGGER.info("Loading [{}] {}", mixinConfig, mixin);
                        }
                        Reflection.mixinClassesField.set(Reflection.configField.get(config), mixins);
                        Reflection.registerConfigurationMethod.invoke(null, config);
                    }
                } catch (Exception e) {
                    LOGGER.error("Unexpected error", e);
                }
            }
        }
       
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

