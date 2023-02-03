package com.gtnewhorizon.gtnhmixins.mixins;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import com.gtnewhorizon.gtnhmixins.Reflection;
import com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;


@Mixin(value = FMLConstructionEvent.class, remap = false)
public class LateMixinOrchestrationMixin {
    private static boolean finished=false;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void beforeConstructing(Object[] eventData, CallbackInfo ci) throws Throwable {
        // We would normally target something in LoadController, but FastCraft causes that class to be loaded too early to mixin,
        // so we instead hijack the very first FMLConstructionEvent that's constructed and run the logic there.
        if (finished)
            return;

        finished = true;

        GTNHMixins.LOGGER.info("Instantiating all @LateMixin annotated and ILateMixinLoader implemented classes...");

        
        ModClassLoader modClassLoader = (ModClassLoader)eventData[0];
        ASMDataTable asmDataTable = (ASMDataTable)eventData[1];
        final Loader loader = Loader.instance();

        final Set<String> loadedMods = loader.getIndexedModList().keySet();
        GTNHMixinsCore.LOGGER.info("LoadedMods {}", loadedMods.toString());

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

            final Config config = Config.create(mixinConfig);
            final List<String> mixins = lateLoader.getMixins(loadedMods);
            for(String mixin : mixins) {
                GTNHMixins.LOGGER.info("Loading [{}] {}", mixinConfig, mixin);
            }
            Reflection.mixinClassesField.set(Reflection.configField.get(config), mixins);
            Reflection.registerConfigurationMethod.invoke(null, config);

        }

        // Add all mods to the classpath now so they're available for mixin
        for (ModContainer container : loader.getActiveModList()) {
            modClassLoader.addFile(container.getSource());
        }

        Field transformerField = Proxy.class.getDeclaredField("transformer");
        transformerField.setAccessible(true);
        
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Object transformer = transformerField.get(Launch.classLoader.getTransformers().stream().filter(Proxy.class::isInstance).findFirst().get());
        Object processor = Reflection.processorField.get(transformer);

        final MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        final Extensions extensions = (Extensions)Reflection.extensionsField.get(processor);
        Reflection.selectConfigsMethod.invoke(processor, env);
        Reflection.prepareConfigsMethod.invoke(processor, env, extensions);
    }
}