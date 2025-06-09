package makamys.mixingasm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.Launch;

public class ConfigHelper {
    
    private final String MODID;
    private final Logger LOGGER;
    
    public ConfigHelper(String modid) {
        this.MODID = modid;
        this.LOGGER = LogManager.getLogger(MODID);
    }
    
    public Path getDefaultConfigFilePath(Path relPath) throws IOException {
        String resourceRelPath = Paths.get("assets/" + MODID + "/default_config/").resolve(relPath).toString().replace('\\', '/');
        URL resourceURL = new Object() { }.getClass().getEnclosingClass().getClassLoader().getResource(resourceRelPath);
        
        switch(resourceURL.getProtocol()) {
        case "jar":
            String urlString = resourceURL.getPath();
            int lastExclamation = urlString.lastIndexOf('!');
            String newURLString = urlString.substring(0, lastExclamation);
            return FileSystems.newFileSystem(new File(URI.create(newURLString)).toPath(), null).getPath(resourceRelPath);
        case "file":
            return new File(URI.create(resourceURL.toString())).toPath();
        default:
            return null;
        }
    }
    
    private void copyDefaultConfigFile(Path src, Path dest) throws IOException {
        Files.createDirectories(getParentSafe(dest));
        LOGGER.debug("Copying " + src + " -> " + dest);
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public boolean createDefaultConfigFileIfMissing(File configFile, boolean overwrite) {
        Path configFolderPath = Paths.get(new File(Launch.minecraftHome, "config").getPath());
        Path configFilePath = Paths.get(configFile.getPath());

        Path relPath = configFolderPath.relativize(configFilePath);
        
        if (configFilePath.startsWith(configFolderPath)) {
            try {
                Path defaultConfigPath = getDefaultConfigFilePath(relPath);
                if(Files.isRegularFile(defaultConfigPath)) {
                    if(!configFile.exists() || overwrite) {
                        copyDefaultConfigFile(defaultConfigPath, configFile.toPath());
                    }
                } else if(Files.isDirectory(defaultConfigPath)) {
                    Files.createDirectories(Paths.get(configFile.getPath()));
                    // create contents of directory as well
                    for(Object po : Files.walk(defaultConfigPath).toArray()) {
                        Path destPath = configFile.toPath().resolve(
                                defaultConfigPath.toAbsolutePath().relativize(((Path)po).toAbsolutePath()).toString());
                        if(Files.isRegularFile((Path)po)) {
                            if(!Files.exists(destPath) || overwrite) {
                                copyDefaultConfigFile((Path)po, destPath);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create default config file for " + relPath.toString() + ": " + e.getMessage());
                return false;
            }
        } else {
            LOGGER.debug("Invalid argument for creating default config file: " + relPath.toString()
                    + " (file is not in the config directory)");
            return false;
        }
        return true;
    }
    
    public Path getParentSafe(Path p) {
        if(p == null || p.getParent() == null) {
            return Paths.get("");
        } else {
            return p.getParent();
        }
    }
}
