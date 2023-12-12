package io.github.legacymoddingmc.unimixins.example;

import static io.github.legacymoddingmc.unimixins.example.Constants.LOGGER;

import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Constants.MODID, version = Constants.VERSION)
public class ExampleMod
{   
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        LOGGER.info("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
    }
}
