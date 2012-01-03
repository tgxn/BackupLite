package net.tgxn.bukkit.backup.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
/**
 * String loader for the plugin, provides strings for each event.
 *
 * @author gamerx
 */
public class Strings {
    
    private File configFile;
    private FileConfiguration fileConfiguration;
    
    
    private String[][] theStrings = {
        
        // In-Game Messages.
        {"backupstarted",       "[Backup] Started Backup..."},
        {"backupfinished",      "[Backup] Finished Backup!"},
        {"norights",            "[Backup] You do not have the rights to run this backup!"},
        {"reloadedok",          "[Backup] Reloaded %%ARG%% successfully!"},
        {"updateconf",          "[Backup] Version of file is out of date or missing, Updating..."},
        {"confuptodate",        "[Backup] Config file is already Up-To-Date!"},
        
        
        // Console Messages.
        {"defaultperms",        "No permissions plugin detected, defaulting to OP."},
        {"hookedperms",         "Found and hooked a permissions plugin."},
        {"disbaledauto",        "You have disabled scheduled backups!"},
        {"createbudir",         "Created the folder for backups."},
        {"zipdisabled",         "You have disabled backup compression."},
        
        {"skipworlds",          "Skipping worlds backup, for all worlds."},
        {"disabledworlds",      "Backup is disabled for the following world(s):"},
        
        {"skipplugins",         "Skipping plugin backup, for all plugins."},
        {"disabledplugins",     "Backup is disabled for the following plugin(s):"},
        {"enabledplugins",      "Backup is enabled for the following plugin(s):"},
        {"allpluginsdisabled",  "Plugin backup is on, but no plugins are selected."},

        
        {"abortedbackup",       "Aborted backup as no players online. Next attempt in %%ARG%% minutes."},
        
        {"removeold",           "Removing the following backup(s) due to age:"},
        {"errordateformat",     "Date format incorrect Check configuration!"},
        {"errorcreatetemp",     "Error occurred when trying to backup %%ARG%%.  Backup is possibly incomplete."},
        {"backupfailed",        "An error occured during backup. Please report to an admin!"},
        {"newconfigfile",       "No config file exists, creating default."},
        {"failedtogetpropsver", "Failed to retrieve version from config file, I suggest upgrading!"},
        {"configoutdated",      "Your config file is outdated, run '/backup updateconf' in-game to upgrade it."},
        {"lastbackup",          "Last player left, backing up!"}
    
    };
    
    /**
     * Loads the strings configuration file.
     * If it does not exist, it creates it from defaults.
     * 
     * @param plugin The plugin this is for.
     */
    public Strings(Plugin plugin) {
        
        //fileConfiguration = new File(plugin.getDataFolder(), "strings.yml");
        configFile = new File(plugin.getDataFolder(), "strings.yml");
        
        // Create the new strings file.
        fileConfiguration = new YamlConfiguration();
        //fileConfiguration.load(configFile);
        
        // Attempt to load the strings.
        //strings.load();
        
        for(int pos = 0; pos < theStrings.length; pos++) {
            String key = theStrings[pos][0];
            String value = theStrings[pos][1];
            
            if(key.equals("") || value.equals(""))
                return;
            else
                fileConfiguration.addDefault(key, value);
        }

        /** System Variables **/
        fileConfiguration.addDefault("stringnotfound", "String not found - ");
        fileConfiguration.addDefault("version", plugin.getDescription().getVersion());
        
        // Save the strings file.
        //@TODO Fix saving of this file.
        //fileConfiguration.saveConfiguration();
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
