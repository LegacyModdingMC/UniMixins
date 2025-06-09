pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.neoforged.net/releases")
        }
        maven {
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            url = uri("https://maven.wagyourtail.xyz/releases")
        }
        maven {
            url = uri("https://maven.wagyourtail.xyz/snapshots")
        }
        gradlePluginPortal {
            content {
                excludeGroup("org.apache.logging.log4j")
            }
        }
    }
}

rootProject.name = "UniMixins"

include("common")
include("mixin")
include("compat")
include("compatfuture")
// include("module-mixingasm")
// include("module-spongemixins")
// include("module-mixinbooterlegacy")
// include("module-gasstation")
// include("module-gtnhmixins")
// include("module-mixinextras")
// include("module-all")
