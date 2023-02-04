package com.falsepattern.gasstation.mixins;

import com.falsepattern.gasstation.core.GasStationCore;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DevMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        boolean isDev = false;
        try {
            if (Launch.classLoader.getClassBytes("net.minecraft.world.World") != null)
                isDev = true;
        } catch (IOException ignored) {}
        if (isDev) {
            GasStationCore.LOGGER.info("Development environment detected! Loading dev hotfixes...");
            return Arrays.asList("dev.LoaderMixin", "dev.ModDiscovererMixin");
        } else {
            GasStationCore.LOGGER.info("Development environment NOT detected! Skipping dev hotfixes...");
            return Collections.emptyList();
        }
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
