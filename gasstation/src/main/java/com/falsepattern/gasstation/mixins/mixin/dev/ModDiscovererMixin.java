package com.falsepattern.gasstation.mixins.mixin.dev;

import com.falsepattern.gasstation.mixins.IModDiscovererMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.discovery.ContainerType;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;

import java.io.File;
import java.util.List;

@Mixin(value = ModDiscoverer.class,
       remap = false)
public abstract class ModDiscovererMixin implements IModDiscovererMixin {
    @Shadow private List<ModCandidate> candidates;

    @Override
    public List<ModCandidate> getCandidates() {
        return candidates;
    }

    @Inject(method = "findClasspathMods",
            at = @At(value = "INVOKE",
                     target = "Lcpw/mods/fml/common/FMLLog;finer(Ljava/lang/String;[Ljava/lang/Object;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void smartCheck(ModClassLoader modClassLoader, CallbackInfo ci, List<String> knownLibraries, File[] minecraftSources, int i) {
        FMLLog.fine("Found a minecraft related file at %s, examining for mod candidates", minecraftSources[i].getAbsolutePath());
        candidates.add(new ModCandidate(minecraftSources[i], minecraftSources[i], ContainerType.JAR, i == 0, true));
    }

    @Redirect(method = "findClasspathMods",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/FMLLog;finer(Ljava/lang/String;[Ljava/lang/Object;)V"),
              require = 1)
    private void noLog(String format, Object[] data) {

    }
}
