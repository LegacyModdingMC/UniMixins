# Developer usage

## RetroFuturaGradle

The easiest way to use UniMixins is to use [GTNH's ExampleMod](https://github.com/GTNewHorizons/ExampleMod1.7.10), which has UniMixins integration built-in.

## ForgeGradle

To migrate a ForgeGradle-based build script to UniMixins, merge the following into your build script.

```gradle
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

def unimixinsVersion = "insert version here"

dependencies {
    // with modern Gradle, you'd need to use implementation + annotationProcessor
    compile("com.github.LegacyModdingMC.UniMixins:-unimixins-all-1.7.10:$unimixinsVersion:dev")
}

// Exclude conflicting transitive dependencies.
// Only needed if you depend on a mod that depends on one of these.
// This may need to be at the end of the build script.

configurations.compile.dependencies.each {
    if (it instanceof ExternalModuleDependency) {
        it.exclude module: "SpongeMixins"
        it.exclude module: "SpongePoweredMixin"
        it.exclude module: "00gasstation-mc1.7.10"
        it.exclude module: "gtnhmixins"
    }
}

runClient {
    arguments += [
        "--tweakClass", "org.spongepowered.asm.launch.MixinTweaker"
    ]
}

runServer {
    arguments += [
        "--tweakClass", "org.spongepowered.asm.launch.MixinTweaker"
    ]
}
```

You can also depend on modules individually:

```gradle
dependencies {
    // One of these (note: this module doesn't have dev jars)
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixin-1.7.10:$unimixinsVersion+unimix.0.12.0-mixin.0.8.5")
    //implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixin-1.7.10:$unimixinsVersion+spongepowered.0.8.5")
    //implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixin-1.7.10:$unimixinsVersion+fabric.0.12.4-mixin.0.8.5")
    //implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixin-1.7.10:$unimixinsVersion+gasmix.0.8.5-gasstation_7")
    //implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixin-1.7.10:$unimixinsVersion+gtnh.0.8.5-GTNH-2")
    
    // You don't need all the modules, only the ones your mod requires.
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-compat-1.7.10:$unimixinsVersion:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-spongemixins-1.7.10:$unimixinsVersion+gtnh.2.0.1:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixinbooterlegacy-1.7.10:$unimixinsVersion+1.2.1:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-gasstation-1.7.10:$unimixinsVersion+0.5.1:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixinextras-1.7.10:$unimixinsVersion+0.1.1:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-gtnhmixins-1.7.10:$unimixinsVersion+2.1.9:dev")
    implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-mixingasm-1.7.10:$unimixinsVersion+0.2.2:dev")
    
    // Needed if not using UniMix or GTNH's Mixin fork
    //implementation("com.github.LegacyModdingMC.UniMixins:-unimixins-devcompat-1.7.10:$unimixinsVersion:dev")
}
```

## Pitfalls

### Shaded ASM package name

Mods which depend on MixinBooterLegacy or GTNHMixins use the MixinBooterLegacy-style shaded ASM package name (`org.spongepowered.libraries.org.objectweb.asm`), whereas UniMix uses the legacy name (`org.spongepowered.asm.lib`). You will have to switch to the latter to make your mod compile.

```patch
-import org.spongepowered.libraries.org.objectweb.asm.tree.ClassNode;
+import org.spongepowered.asm.lib.tree.ClassNode;
```

### `--mods` hack

Some build scripts use the `--mods` program argument to add a duplicate of the mod in order to work around Mixin [preventing mixin mods on the class path from getting loaded as Forge mods.](https://github.com/SpongePowered/Mixin/issues/207) In UniMix and GTNH's Mixin fork, this issue was fixed, so this workaround should be removed since it only causes problems.

```patch
-       arguments += [
-               "--mods=../build/libs/$archivesBaseName-${version}.jar"
-       ]
```
