import org.apache.tools.ant.filters.ReplaceTokens

plugins {
	id("com.gradleup.shadow") version "8.3.6"
}

repositories {
	maven {
		name = "sponge"
		url = uri("https://repo.spongepowered.org/maven/")
	}
	maven {
		url = uri("https://mvn.falsepattern.com/releases")
	}
}

dependencies {
	compileOnly("org.spongepowered:mixin:0.8.5-gasstation_7")
	shadow(project(":common")) {
		isTransitive = false
	}
}

tasks.shadowJar {
	archiveClassifier = ""
	configurations = listOf(project.configurations.shadow.get())

	relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixinbooterlegacy.repackage.common")

	manifest {
		attributes (
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
			"FMLCorePlugin" to "io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin",
		)
	}
}

tasks.jar {
	dependsOn(tasks.shadowJar)
	enabled = false
}

val mblVersion = "1.2.1"
version = "$version+$mblVersion"

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"mblVersion" to mblVersion
		))
	}
}

unimined.minecraft {

}
