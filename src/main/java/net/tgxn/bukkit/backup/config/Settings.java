package net.tgxn.bukkit.backup.config;

import net.tgxn.bukkit.backup.utils.*;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Loads all settings for the plugin.
 * 
 * @author gamerx
 */
public final class Settings {

    private Plugin plugin;
    private Strings strings;
    
    private File configFile;
    private FileConfiguration fileSettingConfiguration;
    
    
    public Settings(Plugin plugin, File configFile, Strings strings) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.strings = strings;
        
        checkAndCreate();
        
        loadProperties();
        
        checkConfigVersion();
    }
    
    public void checkAndCreate() {
        try {
            if (!configFile.exists()) {
                LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                createDefaultSettings();
            }
        } catch (SecurityException | NullPointerException se) {
            LogUtils.exceptionLog(se.getStackTrace());
        }
    }
    
    /**
     * Load the properties from the configFile, create if needed.
     */
    private void loadProperties() {
        
        fileSettingConfiguration = new YamlConfiguration();
        try {
            // Attempt to load configuration.
            fileSettingConfiguration.load(configFile);
        } catch (IOException | InvalidConfigurationException ex) {
            LogUtils.exceptionLog(ex.getStackTrace(), "Failed to load settings.");
            
        }
    }
    
    private void saveProperties() {
        
        // Check they are loaded.
        if (fileSettingConfiguration == null) {
            return;
        }
        
        // Attempt to save configuration to file forcibly.
        try {
            fileSettingConfiguration.save(configFile);
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex.getStackTrace(), "Error saving config file.");
        }
    }
    
    /**
     * Checks configuration version and then based on the outcome, either runs the update, or returns false.
     * 
     * @return False for no update done, True for update done.
     */
    public boolean checkConfigVersion() {
        
        boolean doUpgrade = false;
        
        // Check configuration is loaded.
        if (fileSettingConfiguration != null) {

            // Get the version information from the file.
            String configVersion = fileSettingConfiguration.getString("version", plugin.getDescription().getVersion());
            String pluginVersion = plugin.getDescription().getVersion();

            // Check we got a version from the config file.
            if (configVersion == null) {
                LogUtils.sendLog(strings.getString("failedtogetpropsver"), Level.SEVERE, true);
                doUpgrade = true;
            }

            // Check if the config is outdated.
            if (!configVersion.equals(pluginVersion))
                doUpgrade = true;

            // After we have checked the versions, we have determined that we need to update.
            if (doUpgrade) {
                LogUtils.sendLog(Level.SEVERE, strings.getString("configupdate"));
                doConfigurationUpdate();
            }
        }
        return doUpgrade;
    }
    
    public void doConfigurationUpdate() {
        loadProperties();
        createDefaultSettings();
        fileSettingConfiguration.set("version", this.plugin.getDescription().getVersion());
        saveProperties();
    }
    
    /**
     * Load the properties file from the JAR and place it in the backup DIR.
     */
    private void createDefaultSettings() {
        
        if (configFile.exists()) {
            configFile.delete();
        }
        
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
            LogUtils.exceptionLog(ioe.getStackTrace(), "Error opening stream.");
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
                LogUtils.exceptionLog(ioe.getStackTrace(), "Error closing stream.");
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
        return fileSettingConfiguration.getInt(property, -1);
    }

    /**
     * Gets the value of a boolean property.
     * 
     * @param property The name of the property.
     * @return The value of the property, defaults to true.
     */
    public boolean getBooleanProperty(String property) {
        return fileSettingConfiguration.getBoolean(property, true);
    }

    /**
     * Gets a value of the string property and make sure it is not null.
     * 
     * @param property The name of the property.
     * @return The value of the property.
     */
    public String getStringProperty(String property) {
        return fileSettingConfiguration.getString(property, "");
    }
    
    /**
     * 
     * @return minutes between backups.
     */
    public int getIntervalInMinutes() {
        String settingBackupInterval = getStringProperty("backupinterval");
        
        if(settingBackupInterval.equals("-1")) {
            return 0;
        }
        
        String lastLetter = settingBackupInterval.substring(settingBackupInterval.length()-1, settingBackupInterval.length());
        int amountTime =  Integer.parseInt(settingBackupInterval.substring(0, settingBackupInterval.length()-1));
        switch(lastLetter) {
            case "H": // Hours
                amountTime = (amountTime * 60);
            break;
            case "D": // Days.
                amountTime = (amountTime * 1440);
            break;
            case "W": // Weeks
                amountTime = (amountTime * 10080);
            break;
        }
        return amountTime;
    }
}
