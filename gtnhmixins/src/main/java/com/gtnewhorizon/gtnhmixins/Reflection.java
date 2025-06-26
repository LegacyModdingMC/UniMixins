package com.gtnewhorizon.gtnhmixins;

import io.github.legacymoddingmc.unimixins.common.abstraction.ComparableVersion;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
    /* Reflection Fun */
    public static final Class<?> pluginWrapperClass, mixinsClass, configClass, mixinConfigClass, mixinTransformerClass, mixinProcessorClass, mixinServiceLaunchWrapperClass, mixinEnvironmentClass;
    public static final Field coreModInstanceField, configField, mixinClassesField, processorField, extensionsField, delegatedTransformersField;
    public static final Method registerConfigurationMethod, selectConfigsMethod, prepareConfigsMethod;

    private static final boolean mixin_0_7;
    
    static {
        try {
            mixin_0_7 = new ComparableVersion((String)Launch.blackboard.get("mixin.initialised")).compareTo(new ComparableVersion("0.8")) < 0;

            /* We're referencing classes here in FML or SpongePowered Mixins, these _should_ be safe to call early */

            /* PluginWrapper */
            pluginWrapperClass = Class.forName("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper");
            coreModInstanceField = pluginWrapperClass.getField("coreModInstance");
            coreModInstanceField.setAccessible(true);

            
            /* Mixin */
            mixinsClass = Class.forName("org.spongepowered.asm.mixin.Mixins");
            registerConfigurationMethod = mixinsClass.getDeclaredMethod("registerConfiguration", Config.class);
            registerConfigurationMethod.setAccessible(true);

            /* MixinTransformer */
            mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            if(!mixin_0_7) {
                // 0.8.5
                processorField = mixinTransformerClass.getDeclaredField("processor");
                processorField.setAccessible(true);
            } else {
                // 0.7.11
                processorField = null;
            }

            /* MixinProcessor */
            if(!mixin_0_7) {
                // 0.8.5
                mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
                selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
                selectConfigsMethod.setAccessible(true);

                prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, Extensions.class);
                prepareConfigsMethod.setAccessible(true);

                extensionsField = mixinProcessorClass.getDeclaredField("extensions");
                extensionsField.setAccessible(true);
            } else {
                // 0.7.11
                mixinProcessorClass = null;

                selectConfigsMethod = mixinTransformerClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
                selectConfigsMethod.setAccessible(true);

                prepareConfigsMethod = mixinTransformerClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
                prepareConfigsMethod.setAccessible(true);

                extensionsField = mixinTransformerClass.getDeclaredField("extensions");
                extensionsField.setAccessible(true);
            }

            /* MixinEnvironment / MixinServiceLaunchWrapper */
            mixinServiceLaunchWrapperClass = Class.forName("org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper");
            mixinEnvironmentClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");

            if(!mixin_0_7) {
                // 0.8.5
                delegatedTransformersField = mixinServiceLaunchWrapperClass.getDeclaredField("delegatedTransformers");
                delegatedTransformersField.setAccessible(true);
            } else {
                // 0.7.11
                delegatedTransformersField = mixinEnvironmentClass.getDeclaredField("transformers");
                delegatedTransformersField.setAccessible(true);
            }

            /* Config */
            configClass = Class.forName("org.spongepowered.asm.mixin.transformer.Config");
            configField = configClass.getDeclaredField("config");
            configField.setAccessible(true);

            /* MixinConfig */
            mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
            mixinClassesField = mixinConfigClass.getDeclaredField("mixinClasses");
            mixinClassesField.setAccessible(true);

        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeSelectConfigs(Object transformer, MixinEnvironment env) {
        try {
            if(!mixin_0_7) {
                // 0.8.5
                Object processor = Reflection.processorField.get(transformer);

                final Extensions extensions = (Extensions) Reflection.extensionsField.get(processor);
                selectConfigsMethod.invoke(processor, env);
            } else {
                // 0.7.11
                selectConfigsMethod.invoke(transformer, env);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokePrepareConfigs(Object transformer, MixinEnvironment env) {
        try {
            if(!mixin_0_7) {
                // 0.8.5
                Object processor = Reflection.processorField.get(transformer);

                final Extensions extensions = (Extensions) Reflection.extensionsField.get(processor);
                prepareConfigsMethod.invoke(processor, env, extensions);
            } else {
                // 0.7.11
                prepareConfigsMethod.invoke(transformer, env);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDelegatedTransformersField(Object newValue) {
        try {
            if(!mixin_0_7) {
                // 0.8.5
                delegatedTransformersField.set(MixinService.getService(), newValue);
            } else {
                // 0.7.11
                delegatedTransformersField.set(MixinEnvironment.getCurrentEnvironment(), newValue);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
