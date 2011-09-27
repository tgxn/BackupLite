/*
 *  Copyright (C) 2011 Kilian Gaertner
 *  Modified      2011 Domenic Horner
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

package backup;

import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Properties {

    // The configs.
    public Configuration config;
    
    /**
     * When constructed all properties are loaded. When no properties.yml exists, the
     * default values are used
     */
    public Properties (Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(),  "properties.yml");
        if (!configFile.exists()) {
            System.out.println("[Backup] Couldn't find the config, create a default one!");
            createDefaultSettings(configFile);
        }
        loadProperties(configFile, plugin);
    }

    /**
     * Load the properties.yml from the JAR and place it in the backup DIR.
     * @param configFile The configFile, that needs to be created.
     */
    private void createDefaultSettings (File configFile) {
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        try {
            // open a stream to the config.ini in the jar, because we can only accecs
            // over the class loader
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/res/properties.yml")));
            String line = "";
            bWriter = new BufferedWriter(new FileWriter(configFile));
            // copy the content
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        } // so we can be sure, that the streams are really closed
        finally {
            try {
                if (bReader != null)
                    bReader.close();
                if (bWriter != null)
                    bWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Load the properties from the properties.yml.
     * @param configFile The properties.yml in the servers DIR.
     */
    private void loadProperties (File configFile, Plugin plugin) {
        //Create new configuration file
	config = new Configuration(configFile);
        try{
            config.load();
	} catch(Exception ex){
            ex.printStackTrace(System.out);
        }
        
        String version = config.getString("version", plugin.getDescription().getVersion());
        if (version == null || !version.equals(plugin.getDescription().getVersion()))
            System.out.println("[BACKUP] Your config file is outdated! Please delete your config.ini and the newest will be created!");

    }

    /**
     * Get a value of the integer stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public int getIntProp (String sname) {
        int integer = config.getInt(sname, -1);
        return integer;
    }
    
    /**
     * Get a value of the boolean stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public boolean getBooleanProp (String sname) {
        boolean bool = config.getBoolean(sname, true);
        return bool;
    }

    /**
     * Get a value of the string stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public String getStringProp (String sname) {
        String string = config.getString(sname);
        if(string != null)
            return string;
        else
            return "";
    }
}
