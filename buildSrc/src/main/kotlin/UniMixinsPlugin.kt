import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.*
import com.palantir.gradle.gitversion.VersionDetails
import xyz.wagyourtail.unimined.api.UniminedExtension
import groovy.lang.Closure
import java.io.File

class UniMixinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit {
        project.plugins.apply("java")
        project.plugins.apply("xyz.wagyourtail.unimined")
        project.plugins.apply("com.palantir.git-version")
        project.plugins.apply("com.gradleup.shadow")

        val minecraftVersion = "1.7.10"
        val forgeVersion = "10.13.4.1614-1.7.10"

        project.extensions.configure<JavaPluginExtension>("java") {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }


        project.dependencies {
            add("compileOnly", "net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")
        }

        val versionDetails: Closure<VersionDetails> by project.extra

        project.version = getGitVersion(versionDetails)
        project.group = "io.github.legacymoddingmc"

        val nameSuffix = if (project.name == "all") "" else "-${project.name}"
        project.extensions.configure<BasePluginExtension>("base") {
            archivesName.set("+unimixins$nameSuffix-1.7.10")
        }

        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        val generated = sourceSets.create("generated")
        val genDir = project.layout.buildDirectory.file("genResources")

        generated.resources.srcDir(genDir)

        val generateEmbeddedCorePluginFile = project.tasks.register("generateEmbeddedCorePluginFile") {
            doLast {
                if (project.hasProperty("FMLCorePlugin")) {
                    val corePlugin = project.property("FMLCorePlugin") as String
                    val dir = File(genDir.get().asFile, "META-INF")
                    dir.mkdirs()

                    val embeddedFile = File(dir, "EmbeddedFMLCorePlugins.txt")
                    embeddedFile.writeText(corePlugin)
                }
            }
        }

        project.tasks.named("processResources", ProcessResources::class.java) {
            dependsOn(generateEmbeddedCorePluginFile)

            from("mcmod.info") {
                filter(mapOf("tokens" to mapOf(
                    "minecraftVersion" to minecraftVersion,
                    "uniMixinsVersion" to project.version.toString(),
                    "projectUrl" to "https://github.com/LegacyModdingMC/UniMixins")
                ), ReplaceTokens::class.java)
            }
        }

        project.tasks.withType(ShadowJar::class.java).configureEach {
            from(generated.resources)

            manifest {
                attributes(mapOf(
                    "FMLCorePlugin" to project.findProperty("FMLCorePlugin")
                ).filterValues { it != null })
            }
        }

        project.extensions.configure<UniminedExtension>("unimined") {
            minecraft(sourceSets.getByName("main"), true) {
                version = minecraftVersion

                mappings {
                    searge()
                    mcp("stable", "12-1.7.10")
                }

                minecraftForge {
                    loader(forgeVersion)
                }
            }
        }
    }

    private fun getGitVersion(versionDetails: Closure<VersionDetails>): String {
        val override = System.getenv("VERSION")
        if (override != null) {
            println("VERSION set! Overriding version to $override")
            return override
        }

        val details = versionDetails()
        var ret = details.lastTag
        if (details.commitDistance > 0) ret += "-${details.commitDistance}-${details.gitHash}"
        if (!details.isCleanTag) ret += "-dirty"
        return ret
    }
}
