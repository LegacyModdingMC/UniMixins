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

val project_url = "https://github.com/LegacyModdingMC/UniMixins"

allprojects {
    apply { plugin("java") }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
