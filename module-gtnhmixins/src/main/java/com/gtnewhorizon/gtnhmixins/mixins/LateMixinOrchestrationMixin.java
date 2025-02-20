package com.gtnewhorizon.gtnhmixins.mixins;

import com.google.common.util.concurrent.Runnables;
import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import com.gtnewhorizon.gtnhmixins.Reflection;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import net.minecraft.launchwrapper.Launch;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


@Mixin(value = LoadController.class, remap = false)
public class LateMixinOrchestrationMixin {
    @Inject(method = "distributeStateMessage(Lcpw/mods/fml/common/LoaderState;[Ljava/lang/Object;)V", at = @At(value = "HEAD"))
    private void beforeConstructing(LoaderState state, Object[] eventData, CallbackInfo ci) throws Throwable {
        // This state is where Forge adds mod files to ModClassLoader
        if (state != LoaderState.CONSTRUCTING) {
            return;
        }

        GTNHMixins.LOGGER.info("Instantiating all @LateMixin annotated and ILateMixinLoader implemented classes...");

        
        ModClassLoader modClassLoader = (ModClassLoader)eventData[0];
        ASMDataTable asmDataTable = (ASMDataTable)eventData[1];
        final Loader loader = Loader.instance();

        final Set<String> loadedModsTemp = new HashSet<>();
        loadedModsTemp.addAll(loader.getIndexedModList().keySet());
        loadedModsTemp.addAll(getLiteLoaderMods());
        final Set<String> loadedMods = Collections.unmodifiableSet(loadedModsTemp);
        GTNHMixins.LOGGER.info("LoadedMods {}", loadedMods.toString());

        for (ASMDataTable.ASMData asmData : asmDataTable.getAll(LateMixin.class.getName())) {
            modClassLoader.addFile(asmData.getCandidate().getModContainer()); // Add to path before `newInstance`
            final String mixinClassName = asmData.getClassName().replace('/', '.');
            GTNHMixins.LOGGER.info("Instantiating {} for its mixins.", mixinClassName);
            Class<?> lateMixinClass = Class.forName(mixinClassName);
            if(!ILateMixinLoader.class.isAssignableFrom(lateMixinClass)) {
                GTNHMixins.LOGGER.error("Class {} has the @LateMixin annotation, but does not implement the ILateMixinLoader interface!", mixinClassName);
                continue;
            }
            ILateMixinLoader lateLoader = (ILateMixinLoader)lateMixinClass.newInstance();
            final String mixinConfig = lateLoader.getMixinConfig();
            GTNHMixins.LOGGER.info("Adding {} mixin configuration.", mixinConfig);

            final Config config = Config.create(mixinConfig, null);
            final List<String> mixins = lateLoader.getMixins(loadedMods);
            for(String mixin : mixins) {
                GTNHMixins.LOGGER.info("Loading [{}] {}", mixinConfig, mixin);
            }
            Reflection.mixinClassesField.set(Reflection.configField.get(config), mixins);
            Reflection.registerConfigurationMethod.invoke(null, config);

        }

        ((Runnable)Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing())).run();

        // Add all mods to the classpath now so they're available for mixin
        for (ModContainer container : loader.getActiveModList()) {
            modClassLoader.addFile(container.getSource());
        }

        // Force rebuild of the transformer list
        Reflection.setDelegatedTransformersField(null);

        Field transformerField = Proxy.class.getDeclaredField("transformer");
        transformerField.setAccessible(true);
        
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Object transformer = transformerField.get(Launch.classLoader.getTransformers().stream().filter(Proxy.class::isInstance).findFirst().get());

        final MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        Reflection.invokeSelectConfigs(transformer, env);
        Reflection.invokePrepareConfigs(transformer, env);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> getLiteLoaderMods() {
        Set<String> mods = new HashSet<>();
        try {
            Class<?> LiteLoaderTweaker = Class.forName("com.mumfrey.liteloader.launch.LiteLoaderTweaker");
            Method hasValidMetaData = Class.forName("com.mumfrey.liteloader.interfaces.LoadableMod").getMethod("hasValidMetaData");
            Object instance = FieldUtils.readDeclaredStaticField(LiteLoaderTweaker, "instance", true);
            Object bootstrap = FieldUtils.readDeclaredField(instance, "bootstrap", true);
            Object enumerator = FieldUtils.readDeclaredField(bootstrap, "enumerator", true);
            Map<String, ?> enabledContainers = (Map<String, ?>) FieldUtils.readDeclaredField(enumerator, "enabledContainers", true);
            GTNHMixins.LOGGER.info("LiteLoader present, adding its mods to the list.");
            for(Entry<String, ?> e : enabledContainers.entrySet()) {
                if((boolean) hasValidMetaData.invoke(e.getValue())) {
                    mods.add(e.getKey());
                }
            }
        } catch (ClassNotFoundException e) {
            GTNHMixins.LOGGER.info("LiteLoader not present.");
        } catch (Exception e) {
            GTNHMixins.LOGGER.error("Failed to get LiteLoader mods.", e);
        }
        return mods;
    }
}
