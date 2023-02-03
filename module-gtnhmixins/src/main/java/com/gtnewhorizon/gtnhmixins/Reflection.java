package com.gtnewhorizon.gtnhmixins;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
    /* Reflection Fun */
    public static final Class<?> pluginWrapperClass, mixinsClass, configClass, mixinConfigClass, mixinTransformerClass, mixinProcessorClass;
    public static final Field coreModInstanceField, configField, mixinClassesField, processorField, extensionsField;
    public static final Method registerConfigurationMethod, selectConfigsMethod, prepareConfigsMethod;
    
    static {
        try {
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
            processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);

            /* MixinProcessor */
            mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
            selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);

            prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, Extensions.class);
            prepareConfigsMethod.setAccessible(true);

            extensionsField = mixinProcessorClass.getDeclaredField("extensions");
            extensionsField.setAccessible(true);

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

}
