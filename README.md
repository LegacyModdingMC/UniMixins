# UniMixins

UniMixins is a Mixin loader for Minecraft 1.7.10 designed for maximum compatibility. It aims to combine the features of as many mixin loaders as possible, and its modular nature makes it possible to use along other loaders if necessary.

## Background

Many Mixin loaders exist for 1.7.10, and each one provides a differing set of extra features referred to as *"extras"* on top of providing Mixin.

Since each of these packages is "glued together", different Mixin loaders cannot be used at the same time due to unavoidable feature overlap. UniMixins breaks this tradition by offering each feature as a separate module (but a combined jar is still provided for convenience.)

## Comparison table

Below is a table comparing the feature sets of known Mixin loaders.

Note: A more detailed version of this table is available [here](https://legacymoddingmc.github.io/wiki/#comparison-of-1.7.10-mixin-loaders/).

<picture>
  <source srcset="docs/comparison-chart-dark.png" media="(prefers-color-scheme: dark)">
  <img src="docs/comparison-chart.png">
</picture>

* <sup>[NOP]</sup>: Module does not do anything. [(issue)](https://github.com/FalsePattern/GasStation/issues/15)
* <sup>[SEP]</sup>: Module is available as a separate mod.

## Usage

Download the `-all` jar, and put it in your mods directory. Remove any other conflicting Mixin loaders (refer to the above table). It's important to keep the `00` at the beginning of the file name to avoid [an issue](https://github.com/tox1cozZ/mixin-booter-legacy/issues/1) with mods that shade mixin.

### Advanced usage

The `-modular` zip contains a separate jar for each module. Some modules depend on other modules, see the below list.

The game will crash if modules are missing dependencies. This can be suppressed by adding the `-Dunimixins.skipIntegrityChecks=true` JVM flag.

## List of modules

All modules depend on the Mixin module. Note that the dependents don't necessarily have to come from UniMixins modules, other mods may also be used to provide them.

### Included in `all`-jar

* **Mixin (UniMix)**: Provides a Mixin fork maintained by the UniMixins developers, based on the Fabric fork.
* **SpongeMixins**: Provides an emulation of SpongeMixins's extras.
* **GTNHMixins**: Provides an emulation of GTNHMixins's extras.
    * Depends on SpongeMixins.
* **Compat**: Improves compatibility by fixing incorrect references to Mixin classes (specifically the shaded ASM) in mods.

### Optional

* **Mixin (SpongeMixins)**: Provides the upstream fork of Mixin.
* **Mixin (Fabric)**: Provides the Fabric fork of Mixin.
* **Mixin (GasMix)**: Provides the GasStation fork of Mixin.
* **Mixin (GTNH)**: Provides the GTNH fork of Mixin.

## FAQ

### Why another mixin loader? Haven't you seen that one xkcd strip?

Mixin loaders on 1.7.10 are currently far too fragmented, with different mods requiring different incompatible loaders. This is partly due to conflicts between their developers, and partly because of technical reasons (the loaders are designed in a monolithic way, which invites incompatibility.) This project has neither of those issues.

### Why are you using the Fabric fork of Mixin? Isn't this a Forge mod?

The Fabric fork of Mixin is more actively maintained than the upstream fork, and has some extra fixes. Most of the Fabric fork's changes are not Fabric-specific, and the single class that is is functional without Fabric.

### What do the "extras" do anyway?

* SpongeMixins, Grimoire, MixinBooterLegacy, GasStation and GTNHMixins offer various ways to let you mix into the classes of non-coremods (something Mixin does not natively support.)
* Mixingasm fixes [a Mixin issue](https://github.com/SpongePowered/Mixin/issues/309) that breaks some ASM transformers due to Mixin altering how they are called.
* MixinExtras adds new Mixin features that mods can use to write less intrusive mixins.

### Mixin? mixin? SpongeMixins? SpongePowered? What's the difference?

* [Mixin](https://github.com/SpongePowered/Mixin): a Java library that lets you transform Java classes in a fluent way.
* [SpongePowered](https://spongepowered.org/): the organization that created Mixin (who also developed a modding API called Sponge.)
* [mixin](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---Understanding-Mixin-Architecture#4-only-you-mixins-can-save-mankind): a special Java class defining a "change" that should be applied to a class. These get applied by Mixin.
* [SpongeMixins](https://github.com/TimeConqueror/SpongeMixins): a Mixin loader created by TimeConqueror that has a very generic name.

## Contributing

When editing the source code of modules, please document your changes in the [CREDITS](CREDITS) file. This allows people to easily see what we have changed, and lets other developers know what changes they have to keep in mind when syncing with upstream.

### Useful commands

* `./gradlew module-XXX:build`: builds the module named XXX
* `./gradlew module-all:build`: build an all-jar
* `./gradlew module-all:clean`: clean all modules

### Build flags
* `-PmixinSource`: sets the mixin flavor that should be used to build the Mixin module. Can be set to `spongepowered`, `fabric`, `gasstation`, `gtnh` and `unimix`.
* `-PmixinSourceIsLocal`: use the locally built version of `unimix` when building the Mixin module.

### Roadmap

A rough roadmap of the project is available [here](https://gist.github.com/makamys/5eaf2ebb878b74213630eae122460f00).

## License

UniMixins is licensed under the LGPL v3. See [LICENSE](LICENSE) for the full details.
