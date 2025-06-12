plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

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
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

unimined.minecraft {

}