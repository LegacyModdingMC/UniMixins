package ${package}.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiMainMenu;

@Mixin(GuiMainMenu.class)
abstract class ExampleMixin {
    
    @Inject(method="initGui()V", at=@At("HEAD"))
    private void init(CallbackInfo info) {
        System.out.println("This line is printed by an example mod mixin!");
    }
}
