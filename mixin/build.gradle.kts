import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.unimined.util.capitalized

plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

val enableAltMixin = false
val local = false

val versionBase = version.toString()

val fabricMixinVersion = "0.15.3+mixin.0.8.7"
val unimixMixinVersion = "0.15.4+mixin.0.8.7"
val spongepoweredMixinVersion = "0.8.7"
val gtnhMixinVersion = "0.8.7-GTNH-2"
val gasmixMixinVersion = "0.8.7-gasstation_7"

val asmVersion = "9.7.1"

dependencies {
    compileOnly("org.spongepowered:mixin:$spongepoweredMixinVersion")
    compileOnly(project(":common")) {
        isTransitive = false
    }
}

val flavors = if (enableAltMixin)
    mapOf("spongepowered" to "SpongePowered",
        "fabric" to "Fabric",
        "gasmix" to "GasMix",
        "gtnh" to "GTNH",
        "unimix" to "UniMix")
else
    mapOf("unimix" to "UniMix")

flavors.forEach { it ->
    val mixinFlavor = it.key
    val _flavor = mixinFlavor.capitalized()
    var mixinFlavorClassifier = "UNKNOWN"
    val mixinFlavorCapitalized = it.value
    var mixinVersion = "UNKNOWN"

    val shadowConf = configurations.create("shadow$_flavor")
    val shadowBridgeConf = configurations.create("shadowBridge$_flavor")
    val shadowSourcesConf = configurations.create("shadowSources$_flavor")

    dependencies {
        shadowConf(project(":common")) {
            isTransitive = false
        }
    }

    lateinit var shadowJarTask: TaskProvider<ShadowJar>

    if (mixinFlavor in listOf("fabric", "spongepowered", "unimix", "gtnh")) {
        // Fabric / SpongePowered / UniMix / GTNH

        if (mixinFlavor in listOf("fabric", "unimix")) {
            val uniLocal = mixinFlavor == "unimix" && local
            val uniJitpack = mixinFlavor == "unimix" && !uniLocal

            mixinVersion = if (mixinFlavor == "fabric") fabricMixinVersion else unimixMixinVersion
            if (uniLocal) {
                mixinVersion += "-local"
            }

            // jitpack differences:
            // - domain is com instead of io (for some strange reason)
            // - we have to use _ instead of + in version string because the latter is not supported (for some strange reason)
            // - artifact id is UniMix, not sponge-mixin (this one makes sense at least)

            val mixinGroup = if (mixinFlavor == "fabric") "net.fabricmc" else "${if (uniJitpack) "com" else "io"}.github.legacymoddingmc"
            val mixinArtifactId = if (uniJitpack) "UniMix" else "sponge-mixin"
            val mixinDepVersion = if (uniJitpack) mixinVersion.replace('+', '_') else mixinVersion
            val mixinDep = "$mixinGroup:$mixinArtifactId:$mixinDepVersion"

            mixinFlavorClassifier = "$mixinFlavor.${mixinVersion.replace('+', '-')}"

            dependencies {
                shadowConf(mixinDep) {
                    exclude(group = "org.ow2.asm")
                }
                shadowConf("org.ow2.asm:asm-tree:$asmVersion")
                shadowConf("org.ow2.asm:asm-commons:$asmVersion")
                shadowConf("org.ow2.asm:asm-util:$asmVersion")

                shadowBridgeConf(mixinDep) {
                    isTransitive = false
                }

                shadowSourcesConf("$mixinDep:sources") {
                    isTransitive = false
                }
            }
        } else if (mixinFlavor == "spongepowered") {
            mixinVersion = spongepoweredMixinVersion

            mixinFlavorClassifier = "spongepowered.$mixinVersion"

            dependencies {
                shadowConf("org.spongepowered:mixin:$mixinVersion")
                shadowBridgeConf("org.spongepowered:mixin:$mixinVersion") {
                    isTransitive = false
                }
                shadowConf("com.google.guava:guava:21.0")
                shadowConf("com.google.code.gson:gson:2.2.4")
                shadowConf("org.ow2.asm:asm-tree:$asmVersion")
                shadowConf("org.ow2.asm:asm-commons:$asmVersion")
                shadowConf("org.ow2.asm:asm-util:$asmVersion")

                shadowSourcesConf("org.spongepowered:mixin:$mixinVersion:sources")
            }
        } else if (mixinFlavor == "gtnh") {
            // Adapted from GTNHMixins's build script

            mixinVersion = gtnhMixinVersion
            mixinFlavorClassifier = "gtnh.$mixinVersion"

            dependencies {
                shadowConf("org.spongepowered:mixin:$mixinVersion")
                shadowConf("org.ow2.asm:asm-tree:$asmVersion")
                shadowConf("org.ow2.asm:asm-commons:$asmVersion")
                shadowConf("org.ow2.asm:asm-util:$asmVersion")
                shadowConf("com.google.guava:guava:21.0")
                shadowBridgeConf("org.spongepowered:mixin:$spongepoweredMixinVersion")
                shadowSourcesConf("org.spongepowered:mixin:$spongepoweredMixinVersion:sources")
            }

        }

        // We want to *not* relocate ASM in the bridge classes. So we use a multi-step
        // build procedure:

        // 1. Create relocated Mixin jar, without the bridge classes
        val mixinJarTask = tasks.register<ShadowJar>("mixinJar$_flavor", ShadowJar::class) {
            destinationDirectory = file("build/tmp")
            archiveClassifier = "tmpMixin$_flavor"
            configurations = listOf(shadowConf)

            relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
            if (mixinFlavor != "gtnh") {
                relocate("com.google", "org.spongepowered.libraries.com.google")
            } else {
                // we don't use this ASM package name
                //relocate "org.objectweb.asm", "org.spongepowered.libraries.org.objectweb.asm"
                relocate("com.google.common", "org.spongepowered.libraries.com.google.common")
                relocate("com.google.thirdparty.publicsuffix", "org.spongepowered.libraries.com.google.thirdparty.publicsuffix")
            }

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
        val bridgeJarTask = tasks.register<ShadowJar>("bridgeJar$_flavor", ShadowJar::class) {
            destinationDirectory = file("build/tmp")
            archiveClassifier = "tmpBridge$_flavor"
            configurations = listOf(shadowBridgeConf)

            include("*.jar")
            include("org/spongepowered/asm/bridge/*")
        }

        // 3. Combine the two jars
        shadowJarTask = tasks.register<ShadowJar>("shadowJar$_flavor", ShadowJar::class) {
            version = versionBase + "+" + mixinFlavorClassifier
            from(sourceSets["main"].output) {
                exclude("mcmod.info")
            }

            relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixin.repackage.common")

            dependsOn(mixinJarTask)
            dependsOn(bridgeJarTask)

            from(zipTree(mixinJarTask.get().archiveFile).matching {
                exclude("module-info.class")
                eachFile {
                    if (path.startsWith("META-INF/services/")) {
                        filter({l -> (if (l.startsWith("org.spongepowered.asm.service.modlauncher.")) null else l).toString() })
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
    } else if (mixinFlavor == "gasmix") {
        // GasMix

        mixinVersion = gasmixMixinVersion

        mixinFlavorClassifier = "gasmix.$mixinVersion"

        dependencies {
            add("shadow$_flavor", "org.spongepowered:mixin:$mixinVersion")
            add("shadowSources$_flavor", "org.spongepowered:mixin:$mixinVersion:sources")
        }

        shadowJarTask = tasks.register<ShadowJar>("shadowJar$_flavor", ShadowJar::class) {
            from(sourceSets["main"].output) {
                exclude("mcmod.info")
            }

            version = versionBase + "+" + mixinFlavorClassifier
            archiveClassifier = ""
            configurations = listOf(shadowConf)

            relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixin.repackage.common")
        }

        tasks["jar"].dependsOn(shadowJarTask)
    }

    // Common

    val createMcmodInfoTask = tasks.register("createMcmodInfo$_flavor", Copy::class) {
        outputs.upToDateWhen { false }
        from("src/main/resources/mcmod.info")
        into("build/tmp/mcmod.${mixinFlavor}.info")
        filter { line ->
            line.replace("@MIXIN_CLASSIFIER@", mixinFlavorClassifier)
                .replace("@MIXIN_SOURCE_CAPITALIZED@", mixinFlavorCapitalized)
                .replace("@VERSION@", "$versionBase+$mixinFlavorClassifier")
                .replace("@PROJECT_URL@", ext.get("project_url").toString())
        }
    }

    shadowJarTask {
        dependsOn(createMcmodInfoTask)
        from("build/tmp/mcmod.${mixinFlavor}.info/mcmod.info")

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

    val shadowSourcesJarTask = tasks.register<ShadowJar>("shadowSourcesJar$_flavor", ShadowJar::class) {
        from(sourceSets["main"].allSource)

        version = "$versionBase+$mixinFlavorClassifier"
        archiveClassifier = "sources"
        configurations = listOf(shadowSourcesConf)
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
}

unimined.minecraft {

}