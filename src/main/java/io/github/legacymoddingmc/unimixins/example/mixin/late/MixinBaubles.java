package io.github.legacymoddingmc.unimixins.example.mixin.late;

import static io.github.legacymoddingmc.unimixins.example.Constants.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import baubles.common.Baubles;

// Late mixin example
@Mixin(value=Baubles.class, remap=false)
abstract class MixinBaubles {

    @Inject(method="preInit", at=@At("HEAD"))
    private void init(CallbackInfo info) {
        LOGGER.info("This line is printed by the Baubles example late mixin!");
    }
}
