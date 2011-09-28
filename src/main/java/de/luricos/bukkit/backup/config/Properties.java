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

package de.luricos.bukkit.backup.config;

import de.luricos.bukkit.backup.utils.BackupLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.*;
import java.util.logging.Level;

public class Properties {
    private Configuration config;

    public Properties(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "properties.yml");
        if (!configFile.exists()) {
            BackupLogger.prettyLog(Level.WARNING, false, "Couldn't find a config file, creating default!");
            createDefaultSettings(configFile);
        }

        loadProperties(configFile, plugin);
    }

    /**
     * Load the properties.yml from the JAR and place it in the backup DIR.
     *
     * @param configFile The configFile, that needs to be created.
     */
    private void createDefaultSettings(File configFile) {
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        
        try {
            // open a stream to the property.yml in the jar, because we can only accecs
            // over the class loader
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/res/properties.yml")));
            String line;
            bWriter = new BufferedWriter(new FileWriter(configFile));
            
            // copy the content
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (Exception e) {
            /** @TODO create exception classes **/
            e.printStackTrace(System.out);
        } // so we can be sure, that the streams are really closed
        finally {
            try {
                if (bReader != null)
                    bReader.close();
                if (bWriter != null)
                    bWriter.close();
            } catch (Exception e) {
                /** @TODO create exception classes **/
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Load the properties from the properties.yml.
     *
     * @param configFile The properties.yml in the servers DIR.
     * @param plugin The plugin
     */
    private void loadProperties(File configFile, Plugin plugin) {
        //Create new configuration file
        config = new Configuration(configFile);
        try {
            config.load();
        } catch (Exception ex) {
            /** @TODO create exception classes **/
            ex.printStackTrace(System.out);
        }

        String version = config.getString("version", plugin.getDescription().getVersion());
        if (version == null || !version.equals(plugin.getDescription().getVersion()))
            BackupLogger.prettyLog(Level.SEVERE, false, "Your config file is outdated! Please delete your properties.yml and the newest will be created after a server reload");

    }

    /**
     * Get a value of the integer stored properties
     *
     * @param property see the constants of propertiesSystem
     * @return The value of the property
     */
    public int getIntProperty(String property) {
        return config.getInt(property, -1);
    }

    /**
     * Get a value of the boolean stored properties
     *
     * @param property property see the constants of propertiesSystem
     * @return The value of the property
     */
    public boolean getBooleanProperty(String property) {
        return config.getBoolean(property, true);
    }

    /**
     * Get a value of the string stored properties
     *
     * @param property see the constants of propertiesSystem
     * @return The value of the property
     */
    public String getStringProperty(String property) {
        return config.getString(property, "");
    }
}