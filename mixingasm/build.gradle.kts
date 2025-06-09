plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    implementation(project(":mixin"))
    //implementation(files(tasks.getByPath(":mixin:shadowJarUnimix").getOutputs().getFiles().getSingleFile()))
    shadow(project(":common")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    archiveClassifier = ""
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

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

//ext.uniVersion = version
//version += "+" + ext.extraVersion

unimined.minecraft {

}
