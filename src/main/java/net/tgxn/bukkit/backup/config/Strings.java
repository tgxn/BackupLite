package net.tgxn.bukkit.backup.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.Plugin;


/**
 * String loader for the plugin, provides strings for each event.
 *
 * @author gamerx
 */
public class Strings {
    
    private Plugin plugin;
    private File stringsFile;
    private FileConfiguration fileConfiguration;
    
    /**
     * Loads the strings configuration file.
     * If it does not exist, it creates it from defaults.
     * 
     * @param plugin The plugin this is for.
     */
    public Strings(Plugin plugin) {
        
        this.plugin = plugin;
        stringsFile = new File(plugin.getDataFolder(), "strings.yml");
        
        checkForStringsFile();
        
        loadStrings();

    }
    
    private void checkForStringsFile() {
                // Check for the config file, have it created if needed.
        try {
            if (!stringsFile.exists()) {
                //LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                createDefaultStrings();
            }
        } catch (SecurityException | NullPointerException se) {
            LogUtils.exceptionLog(se.getStackTrace());
        }
    }
    
    private void createDefaultStrings() {
    
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
    
    private void loadStrings() {

        
        // Create the new strings file.
        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(stringsFile);
        } catch (FileNotFoundException ex) {
        } catch (IOException | InvalidConfigurationException ex) {
        }
        
    }
    
    
    /**
     * Gets a value of the string property.
     * 
     * @param sname The identifier for the string.
     * @return The string from properties, with colors encoded.
     */
    public String getString(String property) {
        
        // Get string for this name.
        String string = fileConfiguration.getString(property);
        
        // If we cannot find a string for this, return default.
        if (string != null)
            return colorizeString(string);
        else
            return fileConfiguration.getString("stringnotfound") + property;
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
        String string = fileConfiguration.getString(property);
        
        // If we cannot find a string for this, return default.
        if (string != null)
            return colorizeString(string.replaceAll("%%ARG%%", option));
        else
            return fileConfiguration.getString("stringnotfound") + property;
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
