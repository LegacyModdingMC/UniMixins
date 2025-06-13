import org.apache.tools.ant.filters.ReplaceTokens

val gasstationVersion = "0.5.1"
val gasMixVersion = "0.8.5-gasstation_7"
version = "$version+$gasstationVersion"
ext.set("FMLCorePlugin", "com.falsepattern.gasstation.core.GasStationCore")

repositories {
	maven {
		url = uri("https://mvn.falsepattern.com/releases")
	}
}

dependencies {
	compileOnly("org.spongepowered:mixin:$gasMixVersion")
	compileOnly(project(":mixinbooterlegacy", "shadowArtifact"))
	shadow(project(":common")) {
		isTransitive = false
	}
}

tasks.shadowJar {
	configurations = listOf(project.configurations.shadow.get())
	archiveClassifier = ""
	relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.gasstation.repackage.common")

	manifest {
		attributes (
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
			"MixinConfigs" to "mixins.gasstation.json",
		)
	}
}

tasks.jar {
	enabled = false
	dependsOn(tasks.shadowJar)
}

val shadowArtifact: Configuration by configurations.creating
shadowArtifact.isCanBeConsumed = true

artifacts {
	add("shadowArtifact", tasks["shadowJar"]) {
		builtBy(tasks["shadowJar"])
	}
}

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"gasstationVersion" to gasstationVersion
		))
	}
}

unimined.minecraft {

}
