import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

val enableAltMixin = false
val local = false

val versionBase = version.toString()

val unimixMixinVersion = "0.15.3+mixin.0.8.7"
val spongepoweredMixinVersion = "0.8.7"

val asmVersion = "9.7.1"

repositories {
    maven {
        name = "GTNH Third-Party Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/thirdparty/")
    }
}

val shadowUniMix: Configuration by configurations.creating
val shadowBridgeUniMix: Configuration by configurations.creating
val shadowSourcesUniMix: Configuration by configurations.creating

dependencies {
    compileOnly("org.spongepowered:mixin:$spongepoweredMixinVersion")
    compileOnly(project(":common")) {
        isTransitive = false
    }

    shadowUniMix(project(":common")) {
        isTransitive = false
    }
}

var mixinFlavorClassifier = "UNKNOWN"
val mixinFlavorCapitalized = "UniMix"
val mixinVersion = "$unimixMixinVersion${if (local) "-local" else ""}"

val mixinDep = "io.github.legacymoddingmc:sponge-mixin:$mixinVersion"

mixinFlavorClassifier = "unimix.${mixinVersion.replace('+', '-')}"

dependencies {
    shadowUniMix(mixinDep) {
        exclude(group = "org.ow2.asm")
    }
    shadowUniMix("org.ow2.asm:asm-tree:$asmVersion")
    shadowUniMix("org.ow2.asm:asm-commons:$asmVersion")
    shadowUniMix("org.ow2.asm:asm-util:$asmVersion")

    shadowBridgeUniMix(mixinDep) {
        isTransitive = false
    }

    shadowSourcesUniMix("$mixinDep:sources") {
        isTransitive = false
    }
}

// We want to *not* relocate ASM in the bridge classes. So we use a multi-step
// build procedure:

// 1. Create relocated Mixin jar, without the bridge classes
val mixinJarTask = tasks.register<ShadowJar>("mixinJar$mixinFlavorCapitalized", ShadowJar::class) {
    destinationDirectory = file("build/tmp")
    archiveClassifier = "tmpMixin$mixinFlavorCapitalized"
    configurations = listOf(shadowUniMix)

    relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
    relocate("com.google", "org.spongepowered.libraries.com.google")

    exclude("org/spongepowered/asm/bridge/RemapperAdapter.class")
    exclude("org/spongepowered/asm/bridge/RemapperAdapterFML.class")

    // Exclude stuff that's compiled for Java 16

    exclude("org/spongepowered/asm/service/modlauncher/*")
    exclude("org/spongepowered/asm/launch/MixinTransformationServiceLegacy*")
    exclude("org/spongepowered/asm/launch/MixinLaunchPlugin*")
    exclude("org/spongepowered/asm/launch/MixinTransformationService*")
    exclude("org/spongepowered/asm/launch/platform/container/ContainerHandleModLauncherEx*")

    exclude("META-INF/services/cpw.mods.modlauncher.api.ITransformationService")
    exclude("META-INF/services/cpw.mods.modlauncher.serviceapi.ILaunchPluginService")

    exclude("**/module-info.class")

    // Exclude jar-specific stuff

    exclude("META-INF/MANIFEST.MF", "META-INF/maven/**", "META-INF/*.RSA", "META-INF/*.SF")
}

// 2. Create Mixin jar without relocation, with *only* the bridge classes
val bridgeJarTask = tasks.register<ShadowJar>("bridgeJar$mixinFlavorCapitalized", ShadowJar::class) {
    destinationDirectory = file("build/tmp")
    archiveClassifier = "tmpBridge$mixinFlavorCapitalized"
    configurations = listOf(shadowBridgeUniMix)

    include("*.jar")
    include("org/spongepowered/asm/bridge/*")
}

// 3. Combine the two jars
val shadowJarTask = tasks.register<ShadowJar>("shadowJar$mixinFlavorCapitalized", ShadowJar::class) {
    version = versionBase + "+" + mixinFlavorClassifier
    from(sourceSets["main"].output) {
        exclude("mcmod.info")
    }

    relocate(
        "io.github.legacymoddingmc.unimixins.common",
        "io.github.legacymoddingmc.unimixins.mixin.repackage.common"
    )

    dependsOn(mixinJarTask)
    dependsOn(bridgeJarTask)

    from(zipTree(mixinJarTask.get().archiveFile).matching {
        exclude("module-info.class")
        eachFile {
            if (path.startsWith("META-INF/services/")) {
                filter({ l -> (if (l.startsWith("org.spongepowered.asm.service.modlauncher.")) null else l).toString() })
            }
        }
    })
    from(zipTree(bridgeJarTask.get().archiveFile).matching { include("org/spongepowered/asm/bridge/*") })

    doLast {
        delete(mixinJarTask.get().archiveFile)
        delete(bridgeJarTask.get().archiveFile)
    }
}

tasks["jar"].dependsOn(shadowJarTask)

// Common

val createMcmodInfoTask = tasks.register("createMcmodInfo$mixinFlavorCapitalized", Copy::class) {
    outputs.upToDateWhen { false }
    from("src/main/resources/mcmod.info")
    into("build/tmp/mcmod.unimix.info")
    filter { line ->
        line.replace("@MIXIN_CLASSIFIER@", mixinFlavorClassifier)
            .replace("@MIXIN_SOURCE_CAPITALIZED@", mixinFlavorCapitalized)
            .replace("@VERSION@", "$versionBase+$mixinFlavorClassifier")
            .replace("@PROJECT_URL@", ext.get("project_url").toString())
    }
}

shadowJarTask {
    dependsOn(createMcmodInfoTask)
    from("build/tmp/mcmod.unimix.info/mcmod.info")

    manifest {
        attributes(
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "FMLCorePlugin" to "io.github.legacymoddingmc.unimixins.mixin.MixinCore",
            "Premain-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Agent-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Can-Redefine-Classes" to true,
            "Can-Retransform-Classes" to true,
            "Implementation-Version" to mixinVersion
        )
    }
}

val shadowSourcesJarTask = tasks.register<ShadowJar>("shadowSourcesJar$mixinFlavorCapitalized", ShadowJar::class) {
    from(sourceSets["main"].allSource)

    version = "$versionBase+$mixinFlavorClassifier"
    archiveClassifier = "sources"
    configurations = listOf(shadowSourcesUniMix)
}

tasks["jar"].dependsOn(shadowSourcesJarTask)

/*if (publishModuleToMaven){
    publishing {
        publications {
            create("maven$_flavor", MavenPublication) {
                artifact tasks."shadowJar$_flavor"
                artifact tasks."shadowSourcesJar$_flavor"

                artifactId = archivesBaseName.substring(1) + (mixinFlavor == "unimix" ? "" : "-" + mixinFlavor)
                groupId = mavenGroupId
            }
        }
    }
}*/

unimined.minecraft {

}

tasks.jar {
    enabled = false
}