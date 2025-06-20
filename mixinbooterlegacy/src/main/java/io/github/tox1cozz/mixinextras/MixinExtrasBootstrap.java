package io.github.tox1cozz.mixinextras;

import io.github.tox1cozz.mixinextras.injector.ModifyExpressionValueInjectionInfo;
import io.github.tox1cozz.mixinextras.injector.ModifyReceiverInjectionInfo;
import io.github.tox1cozz.mixinextras.injector.ModifyReturnValueInjectionInfo;
import io.github.tox1cozz.mixinextras.injector.WrapWithConditionInjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class MixinExtrasBootstrap {

    private static boolean initialized = false;

    /**
     * @deprecated As of 0.0.8, as the field becomes kind of pointless when it gets inlined at compile-time.
     * Use {@link #getVersion()} instead.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static final String VERSION = "0.0.12";

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;

            InjectionInfo.register(ModifyExpressionValueInjectionInfo.class);
            InjectionInfo.register(ModifyReceiverInjectionInfo.class);
            InjectionInfo.register(ModifyReturnValueInjectionInfo.class);
            InjectionInfo.register(WrapWithConditionInjectionInfo.class);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}