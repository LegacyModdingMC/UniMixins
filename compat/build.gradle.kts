unimixins {
    fmlCorePlugin = "io.github.legacymoddingmc.unimixins.compat.CompatCore"
}

plugins {
    id("unimixins")
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    shadowImplementation(project(":common")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.compat.repackage.common")

    manifest {
        attributes (
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        )
    }
}

unimined.minecraft {

}