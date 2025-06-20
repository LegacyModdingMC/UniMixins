plugins {
    kotlin("jvm") version "2.1.21"
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://maven.wagyourtail.xyz/releases")
    }
}

dependencies {
    implementation("xyz.wagyourtail.unimined:xyz.wagyourtail.unimined.gradle.plugin:1.3.14")
    implementation("com.palantir.git-version:com.palantir.git-version.gradle.plugin:3.3.0")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:8.3.6")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.apache.ant:ant:1.10.15")
}

gradlePlugin {
    plugins {
        create("unimixinsConfig") {
            id = "unimixinsConfig"
            implementationClass = "UniMixinsExtender"
        }
        create("unimixins") {
            id = "unimixins"
            implementationClass = "UniMixinsPlugin"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}