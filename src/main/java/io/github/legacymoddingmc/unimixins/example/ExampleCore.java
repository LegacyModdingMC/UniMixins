package io.github.legacymoddingmc.unimixins.example;

import static io.github.legacymoddingmc.unimixins.example.Constants.MODID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

// Define early mixins (mixins targetting vanilla, FML or coremods) in this class.
// Defining mixins this way is functionally equivalent to defining them in an IMixinConfigPlugin.
// The benefit of this method is that you are provided with a list of loaded coremods you can check.
public class ExampleCore implements IEarlyMixinLoader, IFMLLoadingPlugin {

    @Override
    public String getMixinConfig() {
        return "mixins." + MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoremods) {
        List<String> mixins = new ArrayList<>();
        if(!loadedCoremods.contains("Bukkit")) {
            mixins.add("early.MixinGuiMainMenu");
        }
        return mixins;
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
