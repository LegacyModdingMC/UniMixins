dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    shadow(project(":common")) {
        isTransitive = false
    }
}

ext.set("FMLCorePlugin", "io.github.legacymoddingmc.unimixins.compat.CompatCore")

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())
    archiveClassifier = ""

    relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.compat.repackage.common")

    manifest {
        attributes (
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        )
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

val shadowArtifact: Configuration by configurations.creating
shadowArtifact.isCanBeConsumed = true

artifacts {
    add("shadowArtifact", tasks["shadowJar"]) {
        builtBy(tasks["shadowJar"])
    }
}

unimined.minecraft {

}