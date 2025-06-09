import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties
import kotlin.math.exp

plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

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

val mixinVersion = "$unimixMixinVersion${if (local) "-local" else ""}"

val mixinDep = "io.github.legacymoddingmc:sponge-mixin:$mixinVersion"

val mixinFlavorClassifier = "unimix.${mixinVersion.replace('+', '-')}"

dependencies {
    compileOnly("org.spongepowered:mixin:$spongepoweredMixinVersion")
    compileOnly(project(":common")) {
        isTransitive = false
    }

    shadowUniMix(project(":common")) {
        isTransitive = false
    }
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

tasks.processResources {
    files("mcmod.info") {
        val props = HashMap<String, String>()
        props["mixinClassifier"] = mixinFlavorClassifier
        props["version"] = "$versionBase+$mixinFlavorClassifier"
        props["projectUrl"] = ext.get("project_url").toString()

        expand(props)
    }
}

// We want to *not* relocate ASM in the bridge classes. So we use a multi-step
// build procedure:

// 1. Create relocated Mixin jar, without the bridge classes
val mixinJarTask = tasks.register<ShadowJar>("mixinJarUniMix", ShadowJar::class) {
    destinationDirectory = file("build/tmp")
    archiveClassifier = "tmpMixinUniMix"
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
val bridgeJarTask = tasks.register<ShadowJar>("bridgeJarUniMix", ShadowJar::class) {
    destinationDirectory = file("build/tmp")
    archiveClassifier = "tmpBridgeUniMix"
    configurations = listOf(shadowBridgeUniMix)

    include("*.jar")
    include("org/spongepowered/asm/bridge/*")
}

// 3. Combine the two jars
tasks.shadowJar {
    // Clear defaults for the shadow jar
    configurations = listOf()
    archiveClassifier = ""

    version = "$versionBase+$mixinFlavorClassifier"
    from(sourceSets["main"].output)

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

    from(zipTree(bridgeJarTask.get().archiveFile).matching {
        include("org/spongepowered/asm/bridge/*")
    })

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

    doLast {
        delete(mixinJarTask.get().archiveFile)
        delete(bridgeJarTask.get().archiveFile)
    }
}

tasks["jar"].dependsOn(tasks.shadowJar)

val shadowSourcesJarTask = tasks.register<ShadowJar>("shadowSourcesJar", ShadowJar::class) {
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
                artifact tasks."shadowSourcesJar"

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