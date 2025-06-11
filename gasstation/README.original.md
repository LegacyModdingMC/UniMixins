# GasStation

GasStation is library that gives mods the ability of loading mixins without embedding the entire SpongePowered Mixins
library inside themselves.

This library is based on SpongeMixins, but with several significant differences:
- This library provides Mixin 0.8.5 instead of 0.7.11 (new mixin features that are not available in mixin 0.7)
- MixinExtras is included, which has some neat QoL utilities that can be used by mod developers.
- The early/late mixin loading feature from mixin-booter-legacy is included as a part of the library
- Mixingasm is included

GasStation is designed to be 100% backwards compatible with SpongeMixins, and in most cases it's a simple delete and
drag&drop upgrade. However, mods designed to run on GasStation *might not* work on SpongeMixins if they use any of the
extra features included in this library.

Integrated into the buildscript of https://github.com/FalsePattern/ExampleMod1.7.10

### Important: do not remove the 00 prefix from the jar, it's used for making forge load this mod first before others (alphabetical sorting when loading coremods)

## Licenses

GasStation is licensed under LGPLv3.<br>
Full license notice here: https://github.com/FalsePattern/GasStation/blob/master/LICENSE

### Embedded code licenses:
SpongePowered Mixins is licensed under MIT, and is compatible with LGPLv3.<br>
Full license notice here: https://github.com/SpongePowered/Mixin/blob/master/LICENSE.txt

MixinExtras is licensed under MIT, and is compatible with LGPLv3.<br>
Full license notice here: https://github.com/LlamaLad7/MixinExtras/blob/master/LICENSE

MixinBooterLegacy is licensed under LGPLv2.1+, and is compatible with LGPLv3.<br>
Full license notice here: https://github.com/tox1cozZ/mixin-booter-legacy/blob/master/LICENSE

SpongeMixins is licensed under MIT, and is compatible with LGPLv3.<br>
Full license notice here: https://github.com/TimeConqueror/SpongeMixins/blob/master/LICENSE

Mixingasm is licensed under Unlicense, and is compatible with LGPLv3.<br>
Full license notice here: https://github.com/makamys/Mixingasm/blob/master/UNLICENSE