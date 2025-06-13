import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.filter

val gtnhMixinsVersion = "2.2.0"
val mixinExtrasVersion = "0.1.1"

val shadowMixinExtras by configurations.creating
ext.set("FMLCorePlugin", "com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore")

repositories {
	maven {
		name = "GTNH Third-Party Maven"
		url = uri("https://nexus.gtnewhorizons.com/repository/thirdparty/")
	}
}

dependencies {
	compileOnly("org.spongepowered:mixin:0.8.7")
	shadow(project(":common")) {
		isTransitive = false
	}
	compileOnly(project(":mixingasm", "shadowArtifact")) {
		isTransitive = false
	}
	compileOnly("com.github.LlamaLad7:MixinExtras:$mixinExtrasVersion")
	shadowMixinExtras("com.github.LlamaLad7:MixinExtras:$mixinExtrasVersion")
}

// Create the mixinExtrasJar ShadowJar task
val mixinExtrasJar by tasks.registering(ShadowJar::class) {
	destinationDirectory = file("build/tmp")
	archiveClassifier = "mixinExtras"
	archiveVersion = mixinExtrasVersion
	configurations = listOf(shadowMixinExtras)

	relocate("com.llamalad7.mixinextras", "com.gtnewhorizon.mixinextras")
	relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
}

// Configure the main shadowJar
tasks.shadowJar {
	archiveClassifier = ""
	configurations = listOf(project.configurations.shadow.get())

	relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
	relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.gtnhmixins.repackage.common")

	from(zipTree(mixinExtrasJar.get().archiveFile)) {
		eachFile {
			path = "data/legacy_gtnh_mixinextras/" + path.replace(".class", ".klass")
		}
	}

	manifest {
		attributes(
			mapOf(
				"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
				"FMLCorePluginContainsFMLMod" to true,
				"ForceLoadAsMod" to true,
				"MixinConfigs" to "mixins.gtnhmixins.json",
			)
		)
	}
}

// Disable regular JAR and depend on shadowJar
tasks.jar {
	dependsOn(tasks.shadowJar)
	enabled = false
}

tasks.shadowJar {
	dependsOn(mixinExtrasJar)
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
			"gtnhMixinsVersion" to gtnhMixinsVersion,
		))
	}
}

unimined.minecraft {
	//replaceIn("GTNHMixins.java")
	//replace("GRADLETOKEN_VERSION", extraVersion)
}
