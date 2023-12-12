package io.github.legacymoddingmc.unimixins.example.mixin.early;

import static io.github.legacymoddingmc.unimixins.example.Constants.LOGGER;

import java.util.Date;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.GuiMainMenu;

// Early mixin and MixinExtras example
@Mixin(GuiMainMenu.class)
abstract class MixinGuiMainMenu {
    
    @WrapOperation(
            at = @At(target = "Ljava/util/Date;<init>()Ljava/util/Date;", value = "NEW"),
            method = "initGui")
    private Date createNewYearsCalendar(Operation<Date> original) {
        LOGGER.info("This line is printed by the MixinExtras example early mixin!");
        return new Date(0l);
    }
    
}
