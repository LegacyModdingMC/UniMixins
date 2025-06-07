plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    // TODO only include IFMLLoadingPlugin, Mod and ComparableVersion
    compileOnly("net.minecraftforge:forge:1.12.2-14.23.5.2860:universal")
}

unimined.minecraft {

}