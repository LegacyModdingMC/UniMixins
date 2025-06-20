import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

unimixins {
	fmlCorePlugin = "io.github.legacymoddingmc.unimixins.mixinextras.MixinExtrasCore"
}

plugins {
	id("unimixins")
}

val mixinExtrasVersion = "0.4.1"
val shadowSources by configurations.creating

dependencies {
	shadow("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
	shadowSources("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion:sources")
}
version = "$version+$mixinExtrasVersion"

tasks.shadowJar {
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

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"mixinExtrasVersion" to mixinExtrasVersion
		))
	}
}

unimined.minecraft {

}
