import xyz.wagyourtail.unimined.api.unimined

// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("java")
    id("xyz.wagyourtail.unimined") version "1.3.14" apply false
    id("com.palantir.git-version") version "3.3.0" apply false
}

// Mod properties
val modid = "unimixins"
group = "io.github.legacymoddingmc"

// Forge properties
val minecraft_version = "1.7.10"
val forge_version = "10.13.4.1614-1.7.10"

subprojects {
    apply(plugin = "java")
    apply(plugin = "xyz.wagyourtail.unimined")
    apply(plugin = "com.palantir.git-version")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        // TODO only include IFMLLoadingPlugin, Mod and ComparableVersion
        compileOnly("net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")
    }

    fun getVersion(): String {
        val override = System.getenv("VERSION")
        if (override != null) {
            print("VERSION set! Overriding version to $override")
            return override
        }

        val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
        // If it exists, grab the last tag. Otherwise it is UNKNOWN
        val details = versionDetails()
        var ret = details.lastTag
        if (details.commitDistance > 0) ret += "-${details.commitDistance}-${details.gitHash}"
        if (!details.isCleanTag) ret += "-dirty"

        return ret
    }

    version = getVersion()
    base.archivesName = "+unimixins-${ if (project.name == "all") "" else project.name }-1.7.10"

    tasks.processResources {
        files("mcmod.info") {
            val props = HashMap<String, String>()
            props["minecraftVersion"] = minecraft_version
            props["uniMixinsVersion"] = version.toString()
            props["projectUrl"] = "https://github.com/LegacyModdingMC/UniMixins"

            expand(props)
        }
    }

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
