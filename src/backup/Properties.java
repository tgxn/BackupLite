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


/**
 *
 * @author Kilian Gaertner
 */
public class Properties implements PropertyConstants {

    /** How big is the int value array*/
    private final int INT_VALUES_SIZE       = 2;
    private final int BOOL_VALUES_SIZE      = 7;
    private final int STRING_VALUES_SIZE    = 4;
    /** Stores every int property*/
    private int[] intValues = new int[INT_VALUES_SIZE];
    /** Stores every bool property*/
    private boolean[] boolValues = new boolean[BOOL_VALUES_SIZE];
    /** Stores every string property */
    private String[] stringValues = new String[STRING_VALUES_SIZE];

    /**
     * When constructed all properties are loaded. When no config.ini exists, the
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
     * Load the default configs from the config.ini , stored in the jar
     * @param configFile The configFile, but not in the jar
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
     * Load the properties from the config.ini
     * @param configFile The config.ini in the servers dir
     */
    private void loadProperties (File configFile, Plugin plugin) {
       
        
                //Create new configuration file
	Configuration config = new Configuration(configFile);
        try{
            config.load();
	} catch(Exception ex){
         ex.printStackTrace(System.out);
        }
            
    
          intValues[INT_BACKUP_INTERVALL] = config.getInt("backupinterval", 15);
          intValues[INT_MAX_BACKUPS] = config.getInt("maxbackups", 50);
          boolValues[BOOL_ONLY_OPS] = config.getBoolean("onlyops", true);
          boolValues[BOOL_BACKUP_ONLY_PLAYER] = config.getBoolean("backuponlywithplayer", true);        
          boolValues[BOOL_ZIP] =  config.getBoolean("zipbackup", true);
          boolValues[BOOL_ACTIVATE_AUTOSAVE] = config.getBoolean("enableautosave", true);
          boolValues[BOOL_BACKUP_WORLDS] = config.getBoolean("backupworlds", true);
          boolValues[BOOL_BACKUP_PLUGINS] = config.getBoolean("backupplugins", true);
          boolValues[BOOL_SUMMARIZE_CONTENT] = config.getBoolean("singlebackup", true);
          stringValues[STRING_NO_BACKUP_WORLDNAMES] = config.getString("skipworlds", "");
          stringValues[STRING_NO_BACKUP_PLUGINS] = config.getString("skipplugins", "");
          stringValues[STRING_CUSTOM_DATE_FORMAT] = config.getString("dateformat", "'%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS'");
          stringValues[STRING_BACKUP_FOLDER] = config.getString("backuppath", "backups");
 
          String version = config.getString("version", plugin.getDescription().getVersion());
          
          if (version == null || !version.equals(plugin.getDescription().getVersion()))
              System.out.println("[BACKUP] Your config file is outdated! Please delete your config.ini and the newest will be created!");
          
        
        
        
    }

    /**
     * Get a value of the integer stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public int getIntProperty (int property) {
        return intValues[property];
    }

    /**
     * Get a value of the boolean stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public boolean getBooleanProperty (int property) {
        return boolValues[property];
    }

    /**
     * Get a value of the string stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public String getStringProperty (int property) {
        return stringValues[property];
    }
}
