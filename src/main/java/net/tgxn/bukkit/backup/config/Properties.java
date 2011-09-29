/*
 *  Backup - CraftBukkit server backup plugin. (continued 1.8+)
 *  @author Lycano <https://github.com/lycano/>
 * 
 *  Backup - CraftBukkit server backup plugin. (continued 1.7+)
 *  @author Domenic Horner <https://github.com/gamerx/>
 *
 *  Backup - CraftBukkit server backup plugin. (original author)
 *  @author Kilian Gaertner <https://github.com/Meldanor/>
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

import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Properties {
    
    public Configuration config;
    public Strings strings;
    
    /**
     * When constructed all properties are loaded. When no properties.yml exists, the
     * default values are used
     * 
     * @param plugin The plugin this is for.
     */
    public Properties (Plugin plugin) {
        strings = new Strings(plugin);
        File configFile = new File(plugin.getDataFolder(), "properties.yml");
        if (!configFile.exists()) {
            System.out.println(strings.getString("newconfigfile"));
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
            String line;
            
            // Open a stream to the properties file in the jar, because we can only access over the class loader.
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/net/tgxn/bukkit/backup/res/properties.yml")));
            bWriter = new BufferedWriter(new FileWriter(configFile));
            
            // Copy the content.
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (Exception e) {
            /** @TODO create exception classes **/
            e.printStackTrace(System.out);
        }
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
     * Load the properties from the configFile.
     * 
     * @param configFile The configuration file for the server.
     * @param plugin The plugin this is for.
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
            System.out.println(strings.getString("configoutdated"));

    }

    /**
     * Gets the value of a integer property.
     * 
     * @param sname The name of the property.
     * @return The value of the property.
     */
    public int getIntProperty(String sname) {
        return config.getInt(sname, -1);
    }
    
    /**
     * Gets the value of a boolean property.
     * 
     * @param sname The name of the property.
     * @return The value of the property.
     */
    public boolean getBooleanProperty(String sname) {
        return config.getBoolean(sname, true);
    }

    /**
     * Gets a value of the string property.
     * 
     * @param sname The name of the property.
     * @return The value of the property.
     */
    public String getStringProperty(String sname) {
        String string = config.getString(sname);
        if(string != null)
            return string;
        else
            return "";
    }
}
