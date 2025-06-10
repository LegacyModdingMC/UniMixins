package io.github.tox1cozz.mixinbooterlegacy;

import com.google.common.util.concurrent.Runnables;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import io.github.tox1cozz.mixinextras.MixinExtrasBootstrap;
import io.github.legacymoddingmc.unimixins.mixinbooterlegacy.MixinBooterLegacyModule;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Name("MixinBooterLegacy")
@MCVersion("1.7.10")
@SortingIndex(Integer.MIN_VALUE + 1)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name("MixinBooterLegacy")
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 1)
public final class MixinBooterLegacyPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

    static {
        LOGGER.info("MixinBootstrap Initializing...");
        MixinBooterLegacyModule.init();
        // Initialize MixinExtras
        MixinExtrasBootstrap.init();
        Mixins.addConfiguration("mixin.mixinbooterlegacy.json");
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

    @Mod(modid = "mixinbooterlegacy", name = "MixinBooterLegacy", version = "@VERSION@", acceptableRemoteVersions = "*")
    @net.minecraftforge.fml.common.Mod(modid = "mixinbooterlegacy", name = "MixinBooterLegacy", version = "@VERSION@", acceptableRemoteVersions = "*")
    public static class Container {

    }
}