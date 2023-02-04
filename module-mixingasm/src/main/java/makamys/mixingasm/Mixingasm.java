package makamys.mixingasm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import makamys.mixingasm.api.IMixinSafeTransformer;
import makamys.mixingasm.api.TransformerInclusions;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class Mixingasm {
    
    public static final String MODID = "mixingasm";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public static void run() {
        List<String> badTransformers = getBadTransformers();
        LOGGER.debug("Excluding transformers: " + badTransformers);
        for(String badTransformer : badTransformers) {
            MixinEnvironment.getCurrentEnvironment().addTransformerExclusion(badTransformer);
        }
    }
    
    private static boolean isValidClassPattern(String pattern) {
        return !pattern.startsWith(":");
    }
    
    private static List<String> getBadTransformers() {
        List<String> dynamicTransformerInclusionPatterns = TransformerInclusions.getTransformerInclusionList();
        LOGGER.debug("Dynamic transformer inclusion pattern list: " + dynamicTransformerInclusionPatterns);
        
        List<String> badTransformers = new ArrayList<>();
        
        List<String> transformerInclusionPatterns = Stream.of(
                readConfig("transformer_inclusion_list_default.txt").stream(),
                readConfig("transformer_inclusion_list.txt").stream(),
                dynamicTransformerInclusionPatterns.stream())
                .flatMap(i -> i)
                .filter(Mixingasm::isValidClassPattern)
                .collect(Collectors.toList());
        
        List<String> transformerExclusionPatterns = 
                readConfig("transformer_exclusion_list.txt").stream()
                .filter(Mixingasm::isValidClassPattern)
                .collect(Collectors.toList());
        
        for(IClassTransformer trans : Launch.classLoader.getTransformers()) {
            String name = trans.getClass().getCanonicalName();
            boolean included = false;
            if((included = (transformerInclusionPatterns.stream().anyMatch(p -> patternMatches(name, p)) || trans instanceof IMixinSafeTransformer))
                    && transformerExclusionPatterns.stream().noneMatch(p -> patternMatches(name, p))) {
                LOGGER.debug("      Trusting transformer " + name);
            } else {
                LOGGER.debug("  Not trusting transformer " + name + (included ? " (because it was excluded via the config)" : ""));
                badTransformers.add(name);
            }
        }
        return badTransformers;
    }
    
    private static boolean patternMatches(String str, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr.replace(".", "\\.").replace("*", ".*"));
        return pattern.matcher(str).matches();
    }
    
    private static List<String> readConfig(String name){
        ConfigHelper helper = new ConfigHelper(MODID);
        File listFile = new File(Launch.minecraftHome, "config/" + MODID + "/" + name);
        
        listFile.getParentFile().mkdirs();
        
        List<String> lines = listFile.exists() ? readConfigLines(listFile) : null;
        boolean overwrite = lines != null && lines.contains(":replaceableFile");
        
        if(lines == null || overwrite) {
            helper.createDefaultConfigFileIfMissing(listFile, overwrite);
            lines = readConfigLines(listFile);
        }
        
        return lines;
    }
    
    private static List<String> readConfigLines(File file){
        try (FileReader fr = new FileReader(file)){
            return IOUtils.readLines(fr).stream()
                    .map(l -> l.contains("#") ? l.substring(0, l.indexOf('#')) : l)
                    .map(l -> l.trim())
                    .filter(l -> !l.isEmpty())
                    .collect(Collectors.toList());
        } catch(Exception e) {
            System.out.println("Failed to read " + file);
            e.printStackTrace();
        }
        return Arrays.asList();
    }
    
}
