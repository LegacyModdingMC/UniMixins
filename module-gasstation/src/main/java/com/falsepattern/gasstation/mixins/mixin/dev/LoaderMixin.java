package com.falsepattern.gasstation.mixins.mixin.dev;

import com.falsepattern.gasstation.mixins.IModDiscovererMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Loader.class,
       remap = false)
public abstract class LoaderMixin {
    @Redirect(method = "identifyMods",
              at = @At(value = "INVOKE",
                     target = "Lcpw/mods/fml/common/discovery/ModDiscoverer;identifyMods()Ljava/util/List;"),
              require = 1)
    private List<ModContainer> removeDuplicateFiles(ModDiscoverer instance) {
        List<ModCandidate> candidates = ((IModDiscovererMixin)instance).getCandidates();
        List<ModCandidate> uniques = new ArrayList<>();
        List<ModCandidate> dupes = new ArrayList<>();
        for(ModCandidate candidate: candidates) {
            File file = candidate.getModContainer().getAbsoluteFile().toPath().normalize().toFile();
            boolean isUnique = true;
            for (ModCandidate uniqueCandidate: uniques) {
                File uniqueFile = uniqueCandidate.getModContainer().getAbsoluteFile().toPath().normalize().toFile();
                if (file.equals(uniqueFile)) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                uniques.add(candidate);
            } else {
                dupes.add(candidate);
            }
        }
        candidates.removeAll(dupes);
        return instance.identifyMods();
    }
}
