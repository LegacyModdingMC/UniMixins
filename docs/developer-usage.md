# Developer usage

UniMix, the mixin fork included in the all jar, currently does not work in dev environments, so you must depend on modules individually in build scripts. 

To depend on UniMixins, merge this into your build script.

> If using RetroFuturaGradle, replace each occurrence of `compile` with `implementation`.

```gradle
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

def unimixinsVersion = "insert version here"

dependencies {    
    annotationProcessor("io.github.legacymoddingmc.unimixins:_unimixins-all-1.7.10:$unimixinsVersion")
    
    // You may specify any Mixin fork other than `unimix` in place of `spongepowered`.
    compile("io.github.legacymoddingmc.unimixins:_unimixins-mixin-1.7.10:$unimixinsVersion+spongepowered-0.8.5")
    
    // You don't need all the modules, only the ones your mod requires.
    compile("io.github.legacymoddingmc.unimixins:_unimixins-compat-1.7.10:$unimixinsVersion")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-spongemixins-1.7.10:$unimixinsVersion+gtnh-2.0.1")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-mixinbooterlegacy-1.7.10:$unimixinsVersion+1.2.0")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-gasstation-1.7.10:$unimixinsVersion+0.5.1")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-mixinextras-1.7.10:$unimixinsVersion+0.2.2")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-gtnhmixins-1.7.10:$unimixinsVersion+2.1.9")
    compile("io.github.legacymoddingmc.unimixins:_unimixins-mixingasm-1.7.10:$unimixinsVersion+0.1.1")
    
    // Needed if not using GTNH's Mixin fork
    compile("io.github.legacymoddingmc.unimixins:_unimixins-devcompat-1.7.10:$unimixinsVersion")
}

// Exclude conflicting transitive dependencies.
// This may need to be at the end of the build script.

configurations.compile.dependencies.each {
    if (it instanceof ExternalModuleDependency) {
        it.exclude module: "SpongeMixins"
        it.exclude module: "SpongePoweredMixin"
        it.exclude module: "00gasstation"
        it.exclude module: "gtnhmixins"
    }
}
```

Generic Mixin stuff (your build script might already have this):

```gradle
ext.outRefMapFile = "${tasks.compileJava.temporaryDir}/${project.modid}.mixin.refmap.json"

jar {
    manifest {
        attributes (
            'MixinConfigs': "${project.modid}.mixin.json",
            'TweakClass': 'org.spongepowered.asm.launch.MixinTweaker',
            'TweakOrder': 0,
            
            // Needed if the mod contains a @Mod annotation
            'FMLCorePluginContainsFMLMod': 'true',
            'ForceLoadAsMod': 'true',
        )
    }
    
    from outRefMapFile;
}

def outSrgFile = "${tasks.compileJava.temporaryDir}/outSrg.srg"

afterEvaluate {
    tasks.compileJava.options.compilerArgs += ["-AreobfSrgFile=${tasks.reobf.srg}", "-AoutSrgFile=${outSrgFile}", "-AoutRefMapFile=${outRefMapFile}"];
}

reobf {
    addExtraSrgFile outSrgFile
}
```
