import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.exclude
import xyz.wagyourtail.unimined.util.capitalized

// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("java")
    id("maven-publish")
    id("xyz.wagyourtail.unimined") version "1.3.14"
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    maven {
        name = "wagyourtail releases"
        url = uri("https://maven.wagyourtail.xyz/releases")
    }
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
    mavenCentral()
}

group = rootProject.properties["group"].toString()
val versionBase = version.toString()
var archivesBaseName = "+unimixins-1.7.10".replace("-1.7.10", "-mixin-1.7.10")

val fabricMixinVersion = "0.15.3+mixin.0.8.7"
val gasmixMixinVersion = "0.8.7-gasstation_7"
val gtnhMixinVersion = "0.8.7-GTNH-2"
val spongepoweredMixinVersion = "0.8.7"
val unimixMixinVersion = "0.15.4+mixin.0.8.7"

val asmVersion = "9.7.1"

val project_url = "https://github.com/LegacyModdingMC/UniMixins"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

lateinit var moduleCommon: SourceSet
lateinit var moduleMixin: SourceSet
sourceSets {
    moduleCommon = create("module-common")
    moduleMixin = create("module-mixin")
}

configurations {
    this[moduleMixin.compileOnlyConfigurationName].extendsFrom(this[moduleCommon.compileOnlyConfigurationName])
}

val flavors = if (project.hasProperty("enableAltMixin"))
    mapOf("spongepowered" to "SpongePowered",
        "fabric" to "Fabric",
        "gasmix" to "GasMix",
        "gtnh" to "GTNH",
        "unimix" to "UniMix")
else
    mapOf("unimix" to "UniMix")

flavors.forEach {
    val mixinFlavor = it.key
    val _flavor = mixinFlavor.capitalized()
    var mixinFlavorClassifier = "UNKNOWN"
    val mixinFlavorCapitalized = it.value
    var mixinVersion = "UNKNOWN"

    val shadowFlavor = configurations.create("shadow$_flavor")
    val shadowBridgeFlavor = configurations.create("shadowBridge$_flavor")
    val shadowSourcesFlavor = configurations.create("shadowSources$_flavor")

    shadowFlavor.isTransitive = false

    dependencies {
        shadowFlavor(moduleCommon.output) // { isTransitive = false }
    }

    lateinit var shadowJarFlavor: TaskProvider<ShadowJar>

    if (mixinFlavor in listOf("fabric", "spongepowered", "unimix", "gtnh")) {
        when (mixinFlavor) {
            in listOf("fabric", "unimix") -> {
                val uniLocal = mixinFlavor == "unimix" && project.hasProperty("local")
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
                    add("shadow$_flavor", mixinDep, {
                        exclude(group = "org.ow2.asm")
                    })
                    add("shadow$_flavor", "org.ow2.asm:asm-tree:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-commons:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-util:$asmVersion")

                    add("shadowBridge$_flavor", mixinDep, {
                        isTransitive = false
                    })

                    add("shadowSources$_flavor", "$mixinDep:sources", {
                        isTransitive = false
                    })
                }
            }
            "spongepowered" -> {
                mixinVersion = spongepoweredMixinVersion

                mixinFlavorClassifier = "spongepowered.$mixinVersion"

                dependencies {
                    add("shadow$_flavor", "org.spongepowered:mixin:$mixinVersion")
                    add("shadowBridge$_flavor", "org.spongepowered:mixin:$mixinVersion", {
                        isTransitive = false
                    })
                    add("shadow$_flavor", "com.google.guava:guava:21.0")
                    add("shadow$_flavor", "com.google.code.gson:gson:2.2.4")
                    add("shadow$_flavor", "org.ow2.asm:asm-tree:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-commons:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-util:$asmVersion")

                    add("shadowSources$_flavor", "org.spongepowered:mixin:$mixinVersion:sources")
                }
            }
            "gtnh" -> {
                // Adapted from GTNHMixins's build script

                mixinVersion = gtnhMixinVersion
                mixinFlavorClassifier = "gtnh.$mixinVersion"

                dependencies {
                    add("shadow$_flavor", "org.spongepowered:mixin:$mixinVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-tree:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-commons:$asmVersion")
                    add("shadow$_flavor", "org.ow2.asm:asm-util:$asmVersion")
                    add("shadow$_flavor", "com.google.guava:guava:21.0")
                    add("shadowBridge$_flavor", "org.spongepowered:mixin:$spongepoweredMixinVersion")
                    add("shadowSources$_flavor", "org.spongepowered:mixin:$spongepoweredMixinVersion:sources")
                }

            }
        }

        // We want to *not* relocate ASM in the bridge classes. So we use a multi-step
        // build procedure:

        // 1. Create relocated Mixin jar, without the bridge classes
        val mixinJarFlavor = tasks.register("mixinJar$_flavor", ShadowJar::class) {
            destinationDirectory = file("build/tmp")
            archiveClassifier = "tmpMixin$_flavor"
            configurations = listOf(shadowFlavor)

            relocate("org.objectweb.asm", "org.spongepowered.asm.lib")
            if (mixinFlavor != "gtnh") {
                relocate("com.google", "org.spongepowered.libraries.com.google")
            } else {
                // we don't use this ASM package name
                //relocate 'org.objectweb.asm', 'org.spongepowered.libraries.org.objectweb.asm'
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
        }.get()

        // 2. Create Mixin jar without relocation, with *only* the bridge classes
        val bridgeJarFlavor = tasks.register("bridgeJar$_flavor", ShadowJar::class) {
            destinationDirectory = file("build/tmp")
            archiveClassifier = "tmpBridge$_flavor"
            configurations = listOf(shadowBridgeFlavor)

            include("*.jar")
            include("org/spongepowered/asm/bridge/*")
        }.get()

        // 3. Combine the two jars
        shadowJarFlavor = tasks.register("shadowJar$_flavor", ShadowJar::class) {
            version = "$versionBase+$mixinFlavorClassifier"
            from(moduleMixin.output) {
                exclude("mcmod.info")
            }

            relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixin.repackage.common")

            dependsOn("mixinJar$_flavor")
            dependsOn("bridgeJar$_flavor")
            dependsOn("mixinJar$_flavor")
            dependsOn("bridgeJar$_flavor")

            from(zipTree(mixinJarFlavor.archiveFile).matching {
                exclude("module-info.class")
                eachFile {
                    if (path.startsWith("META-INF/services/")) {
                        filter({ l -> (if (!l.startsWith("org.spongepowered.asm.service.modlauncher.")) l else null).toString() })
                    }
                }
            })
            from(zipTree(bridgeJarFlavor.archiveFile).matching {
                include("org/spongepowered/asm/bridge/*")
            })

            doLast {
                delete(mixinJarFlavor.archiveFile)
                delete(bridgeJarFlavor.archiveFile)
            }
        }

        tasks["jar"].dependsOn(shadowJarFlavor)
    } else if (mixinFlavor == "gasmix") {
        // GasMix

        mixinVersion = gasmixMixinVersion

        mixinFlavorClassifier = "gasmix.$mixinVersion"

        dependencies {
            add("shadow$_flavor", "org.spongepowered:mixin:$mixinVersion")
            add("shadowSources$_flavor", "org.spongepowered:mixin:$mixinVersion:sources")
        }

        shadowJarFlavor = tasks.register("shadowJar$_flavor", ShadowJar::class) {
            from(moduleMixin.output) {
                exclude("mcmod.info")
            }

            version = "$versionBase+$mixinFlavorClassifier"
            archiveClassifier = ""
            configurations = listOf(shadowFlavor)

            relocate("io.github.legacymoddingmc.unimixins.common", "io.github.legacymoddingmc.unimixins.mixin.repackage.common")
        }

        tasks["jar"].dependsOn(shadowJarFlavor)
    }

    // Common

    val createMcmodInfoFlavor = tasks.register("createMcmodInfo$_flavor", Copy::class) {
        outputs.upToDateWhen { false }
        from("src/main/resources/mcmod.info")
        into("build/tmp/mcmod.${mixinFlavor}.info")
        filter {
            line -> line
                .replace("@MIXIN_CLASSIFIER@", mixinFlavorClassifier)
                .replace("@MIXIN_SOURCE_CAPITALIZED@", mixinFlavorCapitalized)
                .replace("@VERSION@", "$versionBase+$mixinFlavorClassifier")
                .replace("@PROJECT_URL@", project_url)
        }
    }

    shadowJarFlavor {
        dependsOn("createMcmodInfo$_flavor")
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

    val shadowSourcesJarFlavor = tasks.register("shadowSourcesJar$_flavor", ShadowJar::class) {
        from(moduleMixin.allSource)

        version = "$versionBase+$mixinFlavorClassifier"
        archiveClassifier = "sources"
        configurations = listOf(shadowSourcesFlavor)
    }

    tasks["jar"].dependsOn(shadowSourcesJarFlavor)

    // if (false){
    //     publishing {
    //         publications {
    //             create("maven$_flavor", MavenPublication::class) {
    //                 artifact(shadowJarFlavor)
    //                 artifact(shadowSourcesJarFlavor)
    //
    //                 artifactId = archivesBaseName.substring(1) + (if (mixinFlavor == "unimix") "" else "-$mixinFlavor")
    //                 groupId = mavenGroupId
    //             }
    //         }
    //     }
    // }
}

dependencies {
    // TODO only include IFMLLoadingPlugin, Mod and ComparableVersion
    moduleCommon.compileOnlyConfigurationName("net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")
    moduleMixin.implementationConfigurationName(moduleCommon.output)

    moduleMixin.implementationConfigurationName(moduleCommon.output)
    moduleMixin.compileOnlyConfigurationName("org.spongepowered:mixin:$spongepoweredMixinVersion")
}

unimined.minecraft(moduleCommon, moduleMixin) {
    version = project.properties["minecraft_version"].toString()

    mappings {
        searge()
        mcp("stable", "12-1.7.10")
    }

    minecraftForge {
        loader(project.properties["forge_version"].toString())
    }
}
