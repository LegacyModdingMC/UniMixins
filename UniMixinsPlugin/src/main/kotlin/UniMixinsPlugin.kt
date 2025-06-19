import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.palantir.gradle.gitversion.GitVersionCacheService
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.ConfigurationVariantDetails
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import xyz.wagyourtail.unimined.api.UniminedExtension
import java.io.File


@Suppress("unused")
class UniMixinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit {
        val plugins = project.plugins
        plugins.apply("java")
        plugins.apply("xyz.wagyourtail.unimined")
        plugins.apply("com.palantir.git-version")
        plugins.apply("com.gradleup.shadow")

        val extensions = project.extensions
        val minecraftVersion = "1.7.10"
        val forgeVersion = "10.13.4.1614-1.7.10"
        val unimixins = extensions.getByType(UniMixinsExtension::class.java)

        extensions.configure<JavaPluginExtension>("java") {
            it.sourceCompatibility = JavaVersion.VERSION_1_8
            it.targetCompatibility = JavaVersion.VERSION_1_8
            it.toolchain {
                it.languageVersion.set(JavaLanguageVersion.of(8))
            }
        }

        project.dependencies.add("compileOnly", "net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")

        val gitVersion = GitVersionCacheService
            .getSharedGitVersionCacheService(project)
            .get()
        val versionDetails = gitVersion
            .getVersionDetails(project.projectDir, null)
        project.version = getUniMixinsVersion(gitVersion, project)
        project.group = "io.github.legacymoddingmc"

        val nameSuffix = if (project.name == "all") "" else "-${project.name}"
        extensions.configure<BasePluginExtension>("base") {
            it.archivesName.set("+unimixins$nameSuffix-1.7.10")
        }

        val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
        val generated = sourceSets.create("generated")
        val genDir = project.layout.buildDirectory.file("genResources")

        generated.resources.srcDir(genDir)

        val tasks = project.tasks

        val generateEmbeddedCorePluginFile = tasks.register("generateEmbeddedCorePluginFile") {
            it.doLast {
                val fmlCorePlugin = unimixins.fmlCorePlugin.orNull
                if (fmlCorePlugin != null) {
                    val dir = File(genDir.get().asFile, "META-INF")
                    dir.mkdirs()

                    val embeddedFile = File(dir, "EmbeddedFMLCorePlugins.txt")
                    embeddedFile.writeText(fmlCorePlugin)
                }
            }
        }

        tasks.named("processResources", ProcessResources::class.java) { task ->
            task.dependsOn(generateEmbeddedCorePluginFile)

            task.filesMatching("mcmod.info") { copy ->
                copy.filter(mapOf("tokens" to mapOf(
                    "minecraftVersion" to minecraftVersion,
                    "uniMixinsVersion" to project.version.toString(),
                    "projectUrl" to "https://github.com/LegacyModdingMC/UniMixins")),
                    ReplaceTokens::class.java)
            }
        }

        val cfgs = project.configurations
        val shadowImplementation: Configuration = cfgs.maybeCreate("shadowImplementation")
        shadowImplementation.isCanBeConsumed = false
        shadowImplementation.isCanBeResolved = true

        for (config in listOf("compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath")) {
            cfgs.getByName(config).extendsFrom(shadowImplementation)
        }

        val shadowJar = tasks.named("shadowJar", ShadowJar::class.java)
        shadowJar.configure { sJar ->
            sJar.from(generated.resources)
            sJar.configurations = listOf(shadowImplementation)
            sJar.archiveClassifier.set("dev")

            sJar.manifest { manifest ->
                manifest.attributes(mapOf(
                    "Commit-ID" to versionDetails.gitHash,
                    "FMLCorePlugin" to unimixins.uniMixVersion.orNull
                ).filterValues { it != null })
            }

            // Copy licenses to a folder in META-INF, for the merged jar.
            val licenses = listOf("CREDITS", "LICENSE*", "README*")
            sJar.from(project.projectDir) {
                it.include(licenses); it.into("META-INF/licenses/${project.name}")
            }

            // And ensure they end up in the root, too.
            sJar.from(project.projectDir) { it.include(licenses); it.into("") }
        }

        for (outgoingConfig in listOf("runtimeElements", "apiElements")) {
            val outgoing = cfgs.getByName(outgoingConfig)
            outgoing.outgoing.artifacts.clear()
            outgoing.outgoing.artifact(shadowJar)
        }

        tasks.named("jar", Jar::class.java).configure { jar ->
            jar.archiveClassifier.set("dev-preshadow")
        }

        val shadowRuntimeElements = cfgs.getByName("shadowRuntimeElements")
        val javaComponent = project.components.named("java").get() as AdhocComponentWithVariants
        javaComponent.withVariantsFromConfiguration(shadowRuntimeElements, ConfigurationVariantDetails::skip)

        extensions.configure<UniminedExtension>("unimined") {
            it.minecraft(sourceSets.getByName("main"), true) {
                version = minecraftVersion
                defaultRemapJar = false
                defaultRemapSourcesJar = false

                mappings {
                    searge()
                    mcp("stable", "12-1.7.10")
                }

                minecraftForge {
                    loader(forgeVersion)
                }

                remap(tasks.getByName("shadowJar")) {
                    prodNamespace("searge")
                }
            }
        }
    }

    private fun getUniMixinsVersion(versionCacheService: GitVersionCacheService, project: Project): String {
        val override = System.getenv("VERSION")
        if (override != null) {
            println("VERSION set! Overriding version to $override")
            return override
        }

        return versionCacheService.getGitVersion(project.projectDir, null)
    }
}
