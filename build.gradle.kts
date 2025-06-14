// The previous buildscript was spaghetti
// Time will tell if I do better.

// See buildSrc/src/main/kotlin/UniMixinsPlugin for shared config logic.
subprojects {
    apply(plugin = "unimixins")
}
