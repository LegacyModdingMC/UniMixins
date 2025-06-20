// The previous buildscript was spaghetti
// Time will tell if I do better.
plugins {
    id("unimixins") apply false
}

// See buildSrc/src/main/kotlin/UniMixinsPlugin for shared config logic.
subprojects {
    apply(plugin = "unimixinsConfig")

    configure<UniMixinsExtension> {
        uniMixVersion = "0.15.3+mixin.0.8.7"
    }
}
