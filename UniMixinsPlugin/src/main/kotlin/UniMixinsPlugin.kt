import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.palantir.gradle.gitversion.GitVersionCacheService
import com.palantir.gradle.gitversion.VersionDetails
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import xyz.wagyourtail.unimined.api.UniminedExtension
import java.io.File

@Suppress("unused")
class UniMixinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit {
        project.plugins.apply("java")
        project.plugins.apply("xyz.wagyourtail.unimined")
        project.plugins.apply("com.palantir.git-version")
        project.plugins.apply("com.gradleup.shadow")

        val minecraftVersion = "1.7.10"
        val forgeVersion = "10.13.4.1614-1.7.10"
        project.extensions.create("unimixins", UniMixinsExtension::class.java)

        project.extensions.configure<JavaPluginExtension>("java") {
            it.sourceCompatibility = JavaVersion.VERSION_1_8
            it.targetCompatibility = JavaVersion.VERSION_1_8
            it.toolchain {
                it.languageVersion.set(JavaLanguageVersion.of(8))
            }
        }

        project.dependencies.add("compileOnly", "net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")

        val versionDetails = GitVersionCacheService
            .getSharedGitVersionCacheService(project)
            .get()
            .getVersionDetails(project.projectDir, null)
        project.version = getGitVersion(versionDetails)
        project.group = "io.github.legacymoddingmc"

        val nameSuffix = if (project.name == "all") "" else "-${project.name}"
        project.extensions.configure<BasePluginExtension>("base") {
            it.archivesName.set("+unimixins$nameSuffix-1.7.10")
        }

        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        val generated = sourceSets.create("generated")
        val genDir = project.layout.buildDirectory.file("genResources")

        generated.resources.srcDir(genDir)

        val generateEmbeddedCorePluginFile = project.tasks.register("generateEmbeddedCorePluginFile") {
            it.doLast {
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
            it.dependsOn(generateEmbeddedCorePluginFile)

            it.from("mcmod.info") {
                it.filter(mapOf("tokens" to mapOf(
                    "minecraftVersion" to minecraftVersion,
                    "uniMixinsVersion" to project.version.toString(),
                    "projectUrl" to "https://github.com/LegacyModdingMC/UniMixins")
                ), ReplaceTokens::class.java)
            }
        }

        project.tasks.withType(ShadowJar::class.java).configureEach {
            it.from(generated.resources)

            it.manifest {
                it.attributes(mapOf(
                    "Commit-ID" to versionDetails.gitHash,
                    "FMLCorePlugin" to project.findProperty("FMLCorePlugin")
                ).filterValues { it != null })
            }

            // Copy licenses to a folder in META-INF, for the merged jar.
            it.from("CREDITS", "LICENSE*", "README*")
                .into("META-INF/${project.name}")
        }

        project.extensions.configure<UniminedExtension>("unimined") {
            it.minecraft(sourceSets.getByName("main"), true) {
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

    private fun getGitVersion(versionDetails: VersionDetails): String {
        val override = System.getenv("VERSION")
        if (override != null) {
            println("VERSION set! Overriding version to $override")
            return override
        }

        var ret = versionDetails.lastTag
        if (versionDetails.commitDistance > 0) ret += "-${versionDetails.commitDistance}-${versionDetails.gitHash}"
        if (!versionDetails.isCleanTag) ret += "-dirty"
        return ret
    }
}
