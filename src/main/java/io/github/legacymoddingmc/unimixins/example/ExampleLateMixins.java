package io.github.legacymoddingmc.unimixins.example;

import static io.github.legacymoddingmc.unimixins.example.Constants.MODID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

// Define late mixins (mixins targetting non-coremod mod classes) in this class.
// These mixins get loaded after mod classes are put on the classpath, allowing you to mix into them.
@LateMixin
public class ExampleLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins." + MODID + ".late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if(loadedMods.contains("Baubles")) {
            mixins.add("late.MixinBaubles");
        }
        return mixins;
    }

}
