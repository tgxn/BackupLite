/*
 *  Backup - CraftBukkit server Backup plugin (continued)
 *  Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
 *  Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
 *
 *  Backup - CraftBukkit server Backup plugin (original author)
 *  Copyright (C) 2011 Kilian Gaertner <https://github.com/Meldanor/Backup>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.tgxn.bukkit.backup.config;

import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.*;
import java.util.logging.Level;

public class Settings {

    private Configuration config;
    private Strings strings;
    private File configFile;

    /**
     * Main constructor for properties.
     * It detects is the properties file exists, and have it created if need be.
     * 
     * @param plugin The plugin this is for.
     */
    public Settings(Plugin plugin) {

        // Load strings.
        strings = new Strings(plugin);

        // Create the file object used in this class.
        configFile = new File(plugin.getDataFolder(), "config.yml");

        // Check for the config file, have it created if needed.
        try {
            if (!configFile.exists()) {
                LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                createDefaultSettings();
            }
        } catch (SecurityException se) {
            LogUtils.sendLog(Level.SEVERE, "Failed to check config file: Security Exception.");
            //se.printStackTrace(System.out);
        }

        // Load the properties.
        loadProperties(plugin);
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
            LogUtils.sendLog(Level.SEVERE, "Could not create default config.yml: IO Exception.");
            //ioe.printStackTrace(System.out);
        }
        
        // Make sure everything is closed.
        finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (bWriter != null) {
                    bWriter.close();
                }
            } catch (IOException ioe) {
                LogUtils.sendLog(Level.SEVERE, "Failed to close bReader or bWriter: IO Exception.");
                //ioe.printStackTrace(System.out);
            }
        }
    }

    /**
     * Load the properties from the configFile.
     * 
     * @param plugin The plugin this is for.
     */
    private void loadProperties(Plugin plugin) {

        // Create new configuration file.
        config = new Configuration(configFile);

        // Attempt to load configuration.
        config.load();

        // Get version, and log message if out-of-date.
        String version = config.getString("version", plugin.getDescription().getVersion());
        if (version == null || !version.equals(plugin.getDescription().getVersion())) {
            LogUtils.sendLog(Level.WARNING, strings.getString("configoutdated"), true);
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
