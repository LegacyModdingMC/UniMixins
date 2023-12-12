package io.github.legacymoddingmc.unimixins.example.mixin;

import static io.github.legacymoddingmc.unimixins.example.Constants.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiMainMenu;

// Basic mixin example
@Mixin(GuiMainMenu.class)
abstract class MixinGuiMainMenu {
    
    @Inject(method="initGui()V", at=@At("HEAD"))
    private void init(CallbackInfo info) {
        LOGGER.info("This line is printed by the basic example mixin!");
    }
}
