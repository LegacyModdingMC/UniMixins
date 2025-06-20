import org.apache.tools.ant.filters.ReplaceTokens

unimixins {
	fmlCorePlugin = "com.falsepattern.gasstation.core.GasStationCore"
}

plugins {
	id("unimixins")
}

val gasstationVersion = "0.5.1"
val gasMixVersion = "0.8.5-gasstation_7"
version = "$version+$gasstationVersion"

repositories {
	maven {
		url = uri("https://mvn.falsepattern.com/releases")
	}
}

dependencies {
	compileOnly("org.spongepowered:mixin:$gasMixVersion")
	compileOnly(project(":mixinbooterlegacy"))
	shadow(project(":common")) {
		isTransitive = false
	}
}

tasks.shadowJar {
	configurations = listOf(project.configurations.shadow.get())
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

tasks.processResources {
	files("mcmod.info") {
		filter<ReplaceTokens>("tokens" to mapOf(
			"gasstationVersion" to gasstationVersion
		))
	}
}

unimined.minecraft {

}
