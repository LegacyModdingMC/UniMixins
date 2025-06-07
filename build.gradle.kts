// The previous buildscript was spaghetti
// Time will tell if I do better.

// First, we need the mixin module, which means we need Unimixins!
plugins {
    id("xyz.wagyourtail.unimined") version "1.3.14"
}

allprojects {
    apply { plugin("java") }

    group = rootProject.properties["group"].toString()

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}