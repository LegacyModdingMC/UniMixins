import org.apache.tools.ant.filters.ReplaceTokens

dependencies {
    implementation(project(":mixin", "shadowArtifact"))
    implementation(project(":compat", "shadowArtifact"))
    implementation(project(":mixingasm", "shadowArtifact"))
    implementation(project(":spongemixins", "shadowArtifact"))
    implementation(project(":mixinbooterlegacy", "shadowArtifact"))
    implementation(project(":gasstation", "shadowArtifact"))
    implementation(project(":gtnhmixins", "shadowArtifact"))
    implementation(project(":mixinextras", "shadowArtifact"))
}

val moduleList =
    "\\nMixin," +
    "\\nCompat," +
    "\\nMixingasm," +
    "\\nSpongeMixins," +
    "\\nMixinBooterLegacy," +
    "\\nGasStation," +
    "\\nGTNHMixins, and" +
    "\\nMixinExtras."

tasks.processResources {
    files("mcmod.info") {
        filter<ReplaceTokens>("tokens" to mapOf(
            "moduleList" to moduleList
        ))
    }
}

tasks.shadowJar {
    archiveClassifier = ""

    // Exclude errant license files
    exclude("LICENSE*")

    // Merge mcmod.info files
    transform(McmodInfoMerger("unimixins"))

    // Merge list of coremods
    append("META-INF/EmbeddedFMLCorePlugins.txt")

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
        )
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

unimined.minecraft {

}