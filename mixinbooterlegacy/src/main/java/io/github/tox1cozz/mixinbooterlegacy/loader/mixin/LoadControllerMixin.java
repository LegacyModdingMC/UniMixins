package io.github.tox1cozz.mixinbooterlegacy.loader.mixin;

import com.google.common.util.concurrent.Runnables;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.discovery.ASMDataTable;
import io.github.tox1cozz.mixinbooterlegacy.LateMixin;
import io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.Proxy;
import io.github.tox1cozz.mixinbooterlegacy.ILateMixinLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = LoadController.class, remap = false)
public class LoadControllerMixin {

    @Shadow
    private Loader loader;

    @Inject(method = "distributeStateMessage(Lcpw/mods/fml/common/LoaderState;[Ljava/lang/Object;)V",
            at = @At("HEAD"))
    private void beforeConstructing(LoaderState state, Object[] eventData, CallbackInfo ci) throws Throwable {
        // This state is where Forge adds mod files to ModClassLoader
        if (state != LoaderState.CONSTRUCTING) {
            return;
        }

        ModClassLoader modClassLoader = (ModClassLoader)eventData[0];
        ASMDataTable asmDataTable = (ASMDataTable)eventData[1];

        MixinBooterLegacyPlugin.LOGGER.info("Instantiating all ILateMixinLoader implemented classes...");

        for (ASMDataTable.ASMData asmData : asmDataTable.getAll(LateMixin.class.getName())) {
            modClassLoader.addFile(asmData.getCandidate().getModContainer()); // Add to path before `newInstance`
            Class<?> clazz = Class.forName(asmData.getClassName().replace('/', '.'));
            MixinBooterLegacyPlugin.LOGGER.info("Instantiating {} for its mixins.", clazz);

            if (!ILateMixinLoader.class.isAssignableFrom(clazz)) {
                throw new MixinInitialisationError(String.format("The class %s has the LateMixin annotation, but does not implement the ILateMixinLoader interface.", clazz.getName()));
            }

            ILateMixinLoader loader = (ILateMixinLoader)clazz.newInstance();
            for (String mixinConfig : loader.getMixinConfigs()) {
                if (loader.shouldMixinConfigQueue(mixinConfig)) {
                    MixinBooterLegacyPlugin.LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                    Mixins.addConfiguration(mixinConfig);
                    loader.onMixinConfigQueued(mixinConfig);
                }
            }
        }

        ((Runnable)Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing())).run();

        for (ModContainer container : loader.getActiveModList()) {
            modClassLoader.addFile(container.getSource());
        }

        Field transformerField = Proxy.class.getDeclaredField("transformer");
        transformerField.setAccessible(true);
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Object transformer = transformerField.get(Launch.classLoader.getTransformers().stream().filter(Proxy.class::isInstance).findFirst().get());

        Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

        Field processorField = mixinTransformerClass.getDeclaredField("processor");
        processorField.setAccessible(true);
        Object processor = processorField.get(transformer);

        Class<?> mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");

        Method selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
        selectConfigsMethod.setAccessible(true);

        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        selectConfigsMethod.invoke(processor, env);

        try {
            Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
            prepareConfigsMethod.setAccessible(true);
            prepareConfigsMethod.invoke(processor, env);
        } catch (NoSuchMethodException e) { // 0.8.3+
            Class<?> extensionsClass = Class.forName("org.spongepowered.asm.mixin.transformer.ext.Extensions");
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, extensionsClass);
            prepareConfigsMethod.setAccessible(true);

            Field extensionsField = mixinProcessorClass.getDeclaredField("extensions");
            extensionsField.setAccessible(true);
            Object extensions = extensionsField.get(processor);

            //noinspection JavaReflectionInvocation
            prepareConfigsMethod.invoke(processor, env, extensions);
        }
    }
}
