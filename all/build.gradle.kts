import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("unimixins")
}

dependencies {
    // TODO: make this work
    shadowImplementation(project(":mixin"))
    // TODO: make this suck less
    shadowImplementation(project(":compat"));            shadowSources(project(":compat", "sourcesElements"))
    shadowImplementation(project(":mixingasm"));         shadowSources(project(":mixingasm", "sourcesElements"))
    shadowImplementation(project(":spongemixins"));      shadowSources(project(":spongemixins", "sourcesElements"))
    shadowImplementation(project(":mixinbooterlegacy")); shadowSources(project(":mixinbooterlegacy", "sourcesElements"))
    shadowImplementation(project(":gasstation"));        shadowSources(project(":gasstation", "sourcesElements"))
    shadowImplementation(project(":gtnhmixins"));        shadowSources(project(":gtnhmixins", "sourcesElements"))
    shadowImplementation(project(":mixinextras"));       shadowSources(project(":mixinextras", "sourcesElements"))
}

val moduleList =
    "\\nMixin," +
    "Compat," +
    "Mixingasm," +
    "SpongeMixins," +
    "MixinBooterLegacy," +
    "GasStation," +
    "GTNHMixins, and" +
    "MixinExtras."

tasks.processResources {
    files("mcmod.info") {
        filter<ReplaceTokens>("tokens" to mapOf(
            "moduleList" to moduleList
        ))
    }
}

tasks.shadowJar {
    // Exclude errant license files
    exclude("LICENSE*")
    exclude("README.original.md")

    // Merge mcmod.info files
    transform(McmodInfoMerger("unimixins"))

    // Merge list of coremods
    append("META-INF/EmbeddedFMLCorePlugins.txt")

    // Merge service files
    mergeServiceFiles()

    // Only keep the manifest from this jar
    files("META-INF/MANIFEST.MF") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    manifest {
        attributes(
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            // Manually set this, because it doesn't need to be embedded
            "FMLCorePlugin" to "io.github.legacymoddingmc.unimixins.all.AllCore",
            "MixinConfigs" to "mixins.gtnhmixins.json,mixins.gasstation.json,mixins.gtnhmixins.json,mixingasm.mixin.json",
            "Premain-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Agent-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Can-Redefine-Classes" to true,
            "Can-Retransform-Classes" to true,
            "Implementation-Version" to unimixins.uniMixVersion.get()
        )
    }
}

unimined.minecraft {

}