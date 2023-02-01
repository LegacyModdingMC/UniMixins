package io.github.legacymoddingmc.unimixins;

import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Unimixins.MODID, version = Unimixins.VERSION)
public class Unimixins
{
    public static final String MODID = "unimixins";
    public static final String VERSION = "@VERSION@";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
    }
}
