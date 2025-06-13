ext.set("FMLCorePlugin", "io.github.legacymoddingmc.unimixins.compatfuture.CompatFutureCore")

tasks.shadowJar {
    configurations = listOf()
    archiveClassifier = ""
    
    relocate("io.github.legacymoddingmc.unimixins.compat.stub.cpw", "cpw")

    manifest {
        attributes (
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        )
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar.get())
    enabled = false
}

unimined.minecraft {
    
}
