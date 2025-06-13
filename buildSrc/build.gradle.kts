plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:8.3.6")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.apache.ant:ant:1.10.15")
}
