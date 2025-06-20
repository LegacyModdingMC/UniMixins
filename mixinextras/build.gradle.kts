import org.apache.tools.ant.filters.ReplaceTokens

unimixins {
	fmlCorePlugin = "io.github.legacymoddingmc.unimixins.mixinextras.MixinExtrasCore"
}

plugins {
	id("unimixins")
}

val mixinExtrasVersion = "0.4.1"

dependencies {
	shadowImplSources("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
}
version = "$version+$mixinExtrasVersion"

tasks.shadowJar {
	relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
	
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
			"mixinExtrasVersion" to mixinExtrasVersion
		))
	}
}

unimined.minecraft {

}
