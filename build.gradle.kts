import xyz.wagyourtail.unimined.api.unimined

// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("java")
    id("xyz.wagyourtail.unimined") version "1.3.14" apply false
}

// Mod properties
val modid = "unimixins"
val archives_base = "+unimixins"
// http://maven.apache.org/guides/mini/guide-naming-conventions.html
group = "io.github.legacymoddingmc"

// Forge properties
val minecraft_version = "1.7.10"
val forge_version = "10.13.4.1614-1.7.10"

subprojects {
    apply(plugin = "java")
    apply(plugin = "xyz.wagyourtail.unimined")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        // TODO only include IFMLLoadingPlugin, Mod and ComparableVersion
        compileOnly("net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")
    }

    ext.set("project_url", "https://github.com/LegacyModdingMC/UniMixins")

    unimined.minecraft(sourceSets.main.get(), true) {
        version = minecraft_version

        mappings {
            searge()
            mcp("stable", "12-1.7.10")
        }

        minecraftForge {
            loader(forge_version)
        }
    }
}
