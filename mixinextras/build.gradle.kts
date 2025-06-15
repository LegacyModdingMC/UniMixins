import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

val mixinExtrasVersion = "0.4.1"
val shadowSources by configurations.creating

dependencies {
	shadow("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
	shadowSources("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion:sources")
}
version = "$version+$mixinExtrasVersion"

tasks.shadowJar {
	archiveClassifier = ""
	configurations = listOf(project.configurations.shadow.get())
	
	relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
	
	manifest {
		attributes (
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
		)
	}
}

val shadowSourcesJar by tasks.registering(ShadowJar::class) {
	from(sourceSets.main.get().allSource)
	archiveClassifier = "sources"
	configurations = listOf(shadowSources)
}

//tasks.named("sourcesJar") {
//	enabled = false
//	dependsOn(shadowSourcesJar)
//}

tasks.jar {
	enabled = false
	dependsOn(tasks.shadowJar)
}

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"mixinExtrasVersion" to mixinExtrasVersion
		))
	}
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
