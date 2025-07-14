package com.falsepattern.gasstation.mixins;

import cpw.mods.fml.common.discovery.ModCandidate;

import java.util.List;

@Deprecated
public interface IModDiscovererMixin {
    List<ModCandidate> getCandidates();
}
