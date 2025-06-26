# GTNH Mixins
Library that provides mixin functionality
* Includes a shadowed Sponge Powered Mixins fork - Currently 0.8.5-GTNH
* Shades the required libraries that previously made 0.8.5 challenging to use with MC 1.7.10
* Shades [MixinExtras](https://github.com/LlamaLad7/MixinExtras) - For documentation refer to the official [wiki](https://github.com/LlamaLad7/MixinExtras/wiki)
* Adds two additional loading strategies for mixins, inspired by [Mixin Booter Legacy](https://github.com/tox1cozZ/mixin-booter-legacy)
  * [IEarlyMixinLoader](src/main/java/com/gtnewhorizon/gtnhmixins/IEarlyMixinLoader.java) - For Mixins targetting Minecraft, Forge, and CoreMods [Note: Currently also requires IFMLLoadingPlugin for FML to force early loading]
  * [ILateMixinLoader](src/main/java/com/gtnewhorizon/gtnhmixins/ILateMixinLoader.java) & [@LateMixin](src/main/java/com/gtnewhorizon/gtnhmixins/LateMixin.java) - For mixins that target other (non core) mods.  Requires both the interface and the Annotation due to limitations in the 1.7.10 ASMDataTable

## Starting magic with mixins or how to add its config
Create `mixins.<yourmodid>.json` file directly in resources folder.  Example Below.  A mixinPlugin, or mixin list is acceptable here.  For early/late mixins you _must_ use the provided interfaces.
```json
{
  "required": true,
  "minVersion": "0.8.3-GTNH",
  "package": "com.company.mypackage.mixins",
  "refmap": "mixins.yourmodid.refmap.json",
  "target": "@env(DEFAULT)",
  "compatibilityLevel": "JAVA_8"
}
```
### Early Mixins
Create `mixins.<yourmodid>.early.json`.  Have it reference the base refmap.  The mixin package can be the same or different.

### Late Mixins
Create `mixins.<yourmodid>.late.json`.  Have it reference the base refmap.  The mixin package can be the same or different.



## License

### _GTNHMixins_ 
Lesser GNU Public License 3.0 - see [License](LICENSE)

### _SpongeMixins_

Inspired by [SpongeMixins](https://github.com/GTNewHorizons/SpongeMixins) by TimeConqueror.  Includes backwards compatability with previous versions, used under the [MIT License](https://github.com/GTNewHorizons/SpongeMixins/blob/master/LICENSE)

### _Mixin Booter Legacy_

Inspiration and code adapated from https://github.com/tox1cozZ/mixin-booter-legacy under the [LGPL-2.0 License](https://github.com/tox1cozZ/mixin-booter-legacy/blob/master/LICENSE)

### _MixinExtras_

MixinExtras by LlamaLad7 is included in binary files of GTNH Mixins. It is licensed under the [MIT License](https://github.com/LlamaLad7/MixinExtras/blob/master/LICENSE) and can be found at https://github.com/LlamaLad7/MixinExtras

### Credits

Inspiration and skeleton code for the backwards compat with SpongeMixins (MinecraftURLClassPath) derived from [Falsepattern](https://github.com/FalsePattern/) under either the Fair Use Doctrine,  MIT License, or LGPL license. 

mitchej123 for adding the [original MinecraftURLClassPath](https://github.com/GTNewHorizons/SpongeMixins/commit/a8f81842ea7d7cf131191ea41ba58c3cb05b9a3c)!
