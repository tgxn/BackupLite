package net.tgxn.bukkit.backup.config;

import net.tgxn.bukkit.backup.utils.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.*;
import java.util.logging.Level;

/**
 * Loads all settings for the plugin.
 * 
 * @author gamerx
 */
public class Settings {
    
    private File configFile;
    //@TODO Change to YamlConfig
    private Configuration config = null;
    private Plugin plugin;
    private Strings strings;
    public boolean outOfDate = false;
    
    public Settings(Plugin plugin, File configFile, Strings strings) {
        
        this.plugin = plugin;
        this.configFile = configFile;
        this.strings = strings;

        // Load the properties.
        loadProperties();
    }
    
    /**
     * Load the properties from the configFile, create if needed.
     */
    private void loadProperties() {
                
        // Check for the config file, have it created if needed.
        try {
            if (!configFile.exists()) {
                LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                createDefaultSettings();
            }
        } catch (SecurityException se) {
            LogUtils.exceptionLog(se.getStackTrace());
        } catch (NullPointerException npe) {
            LogUtils.exceptionLog(npe.getStackTrace());
        }
        
        config = new Configuration(configFile);
        
        // Attempt to load configuration.
        config.load();
        
        // Check version of the config file.
        checkConfigVersion();
    }
    
    /**
     * Checks the version in the config file, and suggests the user runs the update command.
     */
    public void checkConfigVersion() {

        boolean needToUpdate = false;

        // Check config is loaded.
        if (config != null) {

            // Get the version information.
            String configVersion = config.getString("version", plugin.getDescription().getVersion());
            String pluginVersion = plugin.getDescription().getVersion();

            // Check we got a version from the config file.
            if (configVersion == null) {
                LogUtils.sendLog(Level.SEVERE, strings.getString("failedtogetpropsver"), true);
                needToUpdate = true;
            }

            // Check if the config is outdated.
            if (!configVersion.equals(pluginVersion))
                needToUpdate = true;

            // After we have checked the versions, we have determined that we need to update.
            if (needToUpdate) {
                LogUtils.sendLog(Level.SEVERE, strings.getString("configoutdated"));
                outOfDate = true;
            } else {
                outOfDate = false;
            }
        }
    }
    
    public void doConfUpdate() {
        createDefaultSettings();
        loadProperties();
    }
    
    /**
     * Load the properties file from the JAR and place it in the backup DIR.
     */
    private void createDefaultSettings() {
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        String line;

        try {

            // Open a stream to the properties file in the jar, because we can only access over the class loader.
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/settings/config.yml")));
            bWriter = new BufferedWriter(new FileWriter(configFile));

            // Copy the content to the configfile location.
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe.getStackTrace());
        }
        
        finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (bWriter != null) {
                    bWriter.close();
                }
                
                
                
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace());
            }
        }
    }


    /**
     * Gets the value of a integer property.
     * 
     * @param property The name of the property.
     * @return The value of the property, defaults to -1.
     */
    public int getIntProperty(String property) {
        return config.getInt(property, -1);
    }

    /**
     * Gets the value of a boolean property.
     * 
     * @param property The name of the property.
     * @return The value of the property, defaults to true.
     */
    public boolean getBooleanProperty(String property) {
        return config.getBoolean(property, true);
    }

    /**
     * Gets a value of the string property and make sure it is not null.
     * 
     * @param property The name of the property.
     * @return The value of the property.
     */
    public String getStringProperty(String property) {
        return config.getString(property, "");
    }
}
