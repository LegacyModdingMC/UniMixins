// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("java")
}

// Mod properties
group = "io.github.legacymoddingmc"

subprojects {
    apply(plugin = "unimixins")
}
