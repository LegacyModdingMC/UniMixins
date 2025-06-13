plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
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
