// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("java")
    id("xyz.wagyourtail.unimined") version "1.3.14"
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

lateinit var moduleMixin: SourceSet
sourceSets {
    moduleMixin = create("module-mixin")
}

unimined.minecraft(moduleMixin) {
    version = project.properties["minecraft_version"].toString()

    mappings {
        searge()
        mcp("stable", "12-1.7.10")
    }

    minecraftForge {
        loader(project.properties["forge_version"].toString())
    }
}
