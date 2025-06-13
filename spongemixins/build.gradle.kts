import org.apache.tools.ant.filters.ReplaceTokens

dependencies {
	compileOnly("org.spongepowered:mixin:0.8.5")
}

val extraVersion = "2.0.1"
version = "$version+gtnh.$extraVersion"

tasks.shadowJar {
	archiveClassifier = ""
	configurations = listOf()
	relocate("com.gtnewhorizon", "ru.timeconqueror.spongemixins.repackage.com.gtnewhorizon")
	manifest {
		attributes (
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
		)
	}
}

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"gtnhMixinsVersion" to extraVersion
		))
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
