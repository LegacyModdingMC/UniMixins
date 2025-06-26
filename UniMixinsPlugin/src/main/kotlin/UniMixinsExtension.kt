import org.gradle.api.provider.Property

interface UniMixinsExtension {
    val uniMixVersion: Property<String>
    val fmlCorePlugin: Property<String?>
}