unimixins {
    fmlCorePlugin = "io.github.legacymoddingmc.unimixins.compatfuture.CompatFutureCore"
}

plugins {
    id("unimixins")
}

tasks.shadowJar {
    configurations = listOf()
    
    relocate("io.github.legacymoddingmc.unimixins.compat.stub.cpw", "cpw")

    manifest {
        attributes (
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        )
    }
}

unimined.minecraft {
    
}
