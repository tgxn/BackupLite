package net.tgxn.bukkit.backup.config;

import net.tgxn.bukkit.backup.utils.LogUtils;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * String loader for the plugin, provides strings for each event.
 *
 * @author gamerx
 */
public class Strings {
    
    private Plugin plugin;
    private File stringsFile;
    private FileConfiguration fileStringConfiguration;
    
    /**
     * Loads the strings configuration file.
     * If it does not exist, it creates it from defaults.
     * 
     * @param plugin The plugin this is for.
     */
    public Strings(Plugin plugin, File stringsFile) {
        this.plugin = plugin;
        this.stringsFile = stringsFile;
        
        checkAndCreate();
        loadStrings();
    }

    private void checkAndCreate() {
        // Check for the config file, have it created if needed.
        try {
            if (!stringsFile.exists()) {
                createDefaultStrings();
            }
        } catch (SecurityException | NullPointerException se) {
            LogUtils.exceptionLog(se.getStackTrace(), "Error checking strings file.");
        }
    }
    
    
    
    private void loadStrings() {
        fileStringConfiguration = new YamlConfiguration();
        try {
            fileStringConfiguration.load(stringsFile);
        } catch (FileNotFoundException ex) {
            LogUtils.exceptionLog(ex.getStackTrace(), "Error loading strings file.");
        } catch (IOException | InvalidConfigurationException ex) {
            LogUtils.exceptionLog(ex.getStackTrace(), "Error loading strings file.");
        }
    }
    
    private void saveStrings() {
        if(fileStringConfiguration == null)
            return;
        try {
            fileStringConfiguration.save(stringsFile);
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex.getStackTrace(), "Error saving strings file.");
        }
    }
    
    private void createDefaultStrings() {
    
        if(stringsFile.exists())
            stringsFile.delete();
        
    /**
     * Load the properties file from the JAR and place it in the backup DIR.
     */
    
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        String line;

        try {

            // Open a stream to the properties file in the jar, because we can only access over the class loader.
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/settings/strings.yml")));
            bWriter = new BufferedWriter(new FileWriter(stringsFile));

            // Copy the content to the configfile location.
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe.getStackTrace(), "Error opening streams.");
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
                LogUtils.exceptionLog(ioe.getStackTrace(), "Error closing streams.");
            }
        }
    

    }
    
    public void doConfigUpdate() {
        loadStrings();
        
    }
    
    /**
     * Gets a value of the string property.
     * 
     * @param sname The identifier for the string.
     * @return The string from properties, with colors encoded.
     */
    public String getString(String property) {
        
        // Get string for this name.
        String string = fileStringConfiguration.getString(property);
        
        // If we cannot find a string for this, return default.
        if (string != null)
            return colorizeString(string);
        else
            return fileStringConfiguration.getString("stringnotfound") + property;
    }
    
    /**
     * Gets a value of the string property, and replaces options.
     * 
     * @param property The identifier for the string.
     * @param option The variable to replace %%ARG%% with.
     * @return The string from properties, with colors encoded, and text replaced.
     */
    public String getString(String property, String option) {
        
        // Get string for this name.
        String string = fileStringConfiguration.getString(property);
        
        // If we cannot find a string for this, return default.
        if (string != null)
            return colorizeString(string.replaceAll("%%ARG%%", option));
        else
            return fileStringConfiguration.getString("stringnotfound") + property;
    }
    
    /**
     * Encodes the color codes, and returns the encoded string.
     * If the parameter is blank or null, return blank.
     * 
     * @param toColour The string to encode.
     * @return The encoded string.
     */
    private String colorizeString(String toColor) {
        
        // Check we got passed a string.
        if (toColor != null)
            return toColor.replaceAll("&([0-9a-f])", "\u00A7$1");
        else
            return "";
    }
}
