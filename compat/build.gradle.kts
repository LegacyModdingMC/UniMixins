unimixins {
    fmlCorePlugin = "io.github.legacymoddingmc.unimixins.compat.CompatCore"
}

plugins {
    id("unimixins")
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    shadow(project(":common")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())

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