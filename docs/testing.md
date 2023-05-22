## Production

List of test cases we have used to verify the functionality of modules. An item is checked if all of its children are either known to work, or no mod is known to use them.

All test cases assume default configs for mods.

* [x] SpongeMixins
    * [x] Mod annotation
        * [x] [BugTorch 1.1.8](https://github.com/jss2a98aj/BugTorch/releases/download/1.1.8/bugtorch-1.7.10-1.1.8.jar)
            * [x] When a cobweb is broken with unenchanted shears, a cobweb is dropped.
                * The mod hard depends on SpongeMixins in [its mod annotation](https://github.com/jss2a98aj/BugTorch/blob/95480e11c5ea922f4cc2a7540ca944ef4a9aeaec/src/main/java/jss/bugtorch/core/BugTorchCore.java#L22).
    * [x] Classpath manipulation
        * [x] [BugTorch 1.1.8](https://github.com/jss2a98aj/BugTorch/releases/download/1.1.8/bugtorch-1.7.10-1.1.8.jar) + [Witchery 0.24.1](https://www.curseforge.com/minecraft/mc-mods/witchery/files/2234410)
            * [x] Garlic garlands placed while facing west have correct bounding boxes visible when highlighted.
                * Witchery is not a coremod, so if this mixin works, that means [adding it to the classpath early](https://github.com/jss2a98aj/BugTorch/blob/95480e11c5ea922f4cc2a7540ca944ef4a9aeaec/src/main/java/jss/bugtorch/core/BugTorchMixinPlugin.java#L149-L166) was successful.

* [x] MixinBooterLegacy
    * ❔ Early loader
        * No mods are known to use this.
    * ❔ Late loader
        * No mods are known to use this.
    * ❔ MixinExtras (MixinBooterLegacy package name)
        * No mods are known to use this.

* [x] GasStation
    * [x] Mod annotation
        * [x] [RightProperGUIScale 2.0.2](https://github.com/basdxz/RightProperGUIScale/releases/download/2.0.2/rightproperguiscale-mc1.7.10-2.0.2.jar) + [FalsePatternLib 0.10.14](https://github.com/FalsePattern/FalsePatternLib/releases/download/0.10.14/falsepatternlib-mc1.7.10-0.10.14.jar)
            * [x] GUI Scale slider can be set to fractional values.
                * The mod hard depends on GasStation in [its mod annotation](https://github.com/basdxz/RightProperGUIScale/blob/a14814a00f7f360491f72367c40fcb31341abf7e/src/main/java/com/github/basdxz/rightproperguiscale/Tags.java#L17).
    * ❔ Early loader
        * No mods are known to use this.
    * ❔ Late loader
        * No mods are known to use this.

* [x] MixinExtras
    * ❔ MixinExtras (upstream package name)
        * No mods are known to use this.

* [x] GTNHMixins
    * [x] Mod annotation
        * [x] [Hodgepodge 2.0.27](https://github.com/GTNewHorizons/Hodgepodge/releases/download/2.0.27/hodgepodge-1.7.10-2.0.27.jar) + [GTNHLib 0.0.11](https://github.com/GTNewHorizons/GTNHLib/releases/download/0.0.11/gtnhlib-1.7.10-0.0.11.jar)
            * [x] Chat background is transparent
                * The mod hard depends on GTNHMixins in [its mod annotation](https://github.com/GTNewHorizons/Hodgepodge/blob/035015166f138ab56c5d5c5acd8d13dad79f95fc/src/main/java/com/mitchej123/hodgepodge/Hodgepodge.java#L21).
    * [x] Early loader
        * [x] [Hodgepodge 2.0.27](https://github.com/GTNewHorizons/Hodgepodge/releases/download/2.0.27/hodgepodge-1.7.10-2.0.27.jar) + [GTNHLib 0.1.1](https://github.com/GTNewHorizons/GTNHLib/releases/download/0.0.11/gtnhlib-1.7.10-0.0.11.jar)
            * [x] Chat background is transparent
                * An early loader is used to [load all early mixins](https://github.com/GTNewHorizons/Hodgepodge/blob/035015166f138ab56c5d5c5acd8d13dad79f95fc/src/main/java/com/mitchej123/hodgepodge/core/HodgepodgeCore.java#L35-L50), [including this one](https://github.com/GTNewHorizons/Hodgepodge/blob/2.0.27/src/main/java/com/mitchej123/hodgepodge/mixins/Mixins.java#L98-L103).
    * [x] Late loader
        * [x] [Hodgepodge 2.0.27](https://github.com/GTNewHorizons/Hodgepodge/releases/download/2.0.27/hodgepodge-1.7.10-2.0.27.jar) + [GTNHLib 0.1.1](https://github.com/GTNewHorizons/GTNHLib/releases/download/0.0.11/gtnhlib-1.7.10-0.0.11.jar) + [Baubles 1.0.1.16](https://github.com/GTNewHorizons/Baubles/releases/download/1.0.1.16/Baubles-1.0.1.16.jar)
            * [x] When the inventory is opened while a potion effect is active, the inventory GUI stays centered.
                * A late loader is used to [load all late mixins](https://github.com/GTNewHorizons/Hodgepodge/blob/2.0.27/src/main/java/com/mitchej123/hodgepodge/HodgepodgeLateMixins.java), [including this one](https://github.com/GTNewHorizons/Hodgepodge/blob/2.0.27/src/main/java/com/mitchej123/hodgepodge/mixins/Mixins.java#L489-L494).
    * [x] MixinExtras (GTNHMixins package name)
        * [x] [Hodgepodge 2.0.27](https://github.com/GTNewHorizons/Hodgepodge/releases/download/2.0.27/hodgepodge-1.7.10-2.0.27.jar) + [GTNHLib 0.1.1](https://github.com/GTNewHorizons/GTNHLib/releases/download/0.0.11/gtnhlib-1.7.10-0.0.11.jar)
            * [x] Chat background is transparent
                * The [mixin](https://github.com/GTNewHorizons/Hodgepodge/blob/2.0.27/src/main/java/com/mitchej123/hodgepodge/mixins/early/minecraft/MixinGuiNewChat_TransparentChat.java) implementing this feature uses `@WrapWithCondition`.

* [x] Mixingasm
    * [x] Transformer exclusion
        * [x] [GregTech 6](https://gregtech.overminddl1.com/com/gregoriust/gregtech/gregtech_1.7.10/6.14.20/gregtech_1.7.10-6.14.20.jar) + [CoreTweaks 0.3.0.1+nomixin](https://github.com/makamys/CoreTweaks/releases/download/0.3.0.1/CoreTweaks-1.7.10-0.3.0.1+nomixin.jar) + [BugTorch 1.1.8](https://github.com/jss2a98aj/BugTorch/releases/download/1.1.8/bugtorch-1.7.10-1.1.8.jar)
            * [x] Game reaches title screen
                * Without Mixingasm, this mod combination causes an early crash.

* [x] Compat
    * [x] Remapping
        * [x] [BugTorch 1.1.6.6-GTNH](https://github.com/GTNewHorizons/BugTorch/releases/download/1.1.6.6-GTNH/bugtorch-1.7.10-1.1.6.6-GTNH.jar)
            * [x] When a cobweb is broken with unenchanted shears, a cobweb is dropped.
                * The mod uses the MixinBooterLegacy-style ASM package name (`org.spongepowered.libraries.org.objectweb.asm`) [in its mixin config plugin](https://github.com/GTNewHorizons/BugTorch/blob/3e1758a12bd92d6dabab566ed9a1e811528c655f/src/main/java/jss/bugtorch/core/BugTorchMixinPlugin.java#L13), while UniMixins uses the legacy package name (`org.spongeweb.asm.lib`). If the mod loads, that means the remapping was successful.

## Development

Mods we have gotten running in a development environment.

* [x] Hodgepodge [@ 6e3907f](https://github.com/GTNewHorizons/Hodgepodge/tree/6e3907fbc619a217678734448295880bb736a063)   
    * Uses GTNH build script with RFG
* [x] BugTorch [@ d5d4a51](https://github.com/jss2a98aj/BugTorch/tree/d5d4a51fd0414fb35692c83004ee5d9152e1a3fe)
    * Uses GTNH build script with RFG
* [x] ArchaicFix [@ 1dac308](https://github.com/embeddedt/ArchaicFix/tree/1dac308d2d15abf82a409cb5574b901b54a6f38c)
    * Uses FalsePattern's build script
