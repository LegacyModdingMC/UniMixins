import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class UniMixinsExtender : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("unimixins", UniMixinsExtension::class.java)
    }
}