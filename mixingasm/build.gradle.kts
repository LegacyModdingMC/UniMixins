import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("unimixins")
}

val mixingasmVersion = "0.3"
version = "$version+$mixingasmVersion"

dependencies {
    implementation(project(":mixin"))
    shadow(project(":common")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())

    relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixingasm.repackage.common")

    manifest {
        attributes (
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixingasm.mixin.json",
            "TweakOrder" to 0,
        )
    }
}

tasks.processResources {
    files("mcmod.info") {
        filter<ReplaceTokens>("tokens" to mapOf(
            "mixingasmVersion" to mixingasmVersion
        ))
    }
}

unimined.minecraft {

}
