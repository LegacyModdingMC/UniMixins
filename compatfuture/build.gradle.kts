plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

tasks.shadowJar {
    configurations = listOf()
    archiveClassifier = ""
    
    relocate("io.github.legacymoddingmc.unimixins.compat.stub.cpw", "cpw")

    manifest {
        attributes (
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "FMLCorePlugin" to "io.github.legacymoddingmc.unimixins.compatfuture.CompatFutureCore"
        )
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar.get())
    enabled = false
}

unimined.minecraft {
    
}
