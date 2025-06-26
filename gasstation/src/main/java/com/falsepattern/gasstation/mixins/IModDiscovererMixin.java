package com.falsepattern.gasstation.mixins;

import cpw.mods.fml.common.discovery.ModCandidate;

import java.util.List;

public interface IModDiscovererMixin {
    List<ModCandidate> getCandidates();
}
