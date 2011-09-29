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

import java.io.File;

public class Strings {

    private Configuration strings;
    
    /**
     * Loads the strings.yml file.
     * If it does not exist, it creates it from defaults.
     * 
     * @param plugin The plugin this is for.
     */
    public Strings (Plugin plugin) {
        strings = new Configuration(new File(plugin.getDataFolder(),  "strings.yml"));
        try{
            strings.load();
	} catch(Exception ex){
            /** @TODO create exception classes **/
            ex.printStackTrace(System.out);
        }
        // In-Game
        strings.getString("backupstarted",  "Started Backup..."); // When a backup is started.
        strings.getString("backupfinished", "Finished Backup!"); // When a backup completes.
        strings.getString("norights",       "You do not have the rights to run this backup!"); // If the player does not have the correct rights.
        
        // Console
        strings.getString("defaultperms",   "[Backup] Permissions system not detected, defaulting to OP."); // Onload permissions check.
        strings.getString("hookedperms",    "[Backup] Found and hooked permissions plugin."); // If we found the Permissions plugin
        strings.getString("disbaledauto",   "[Backup] You have disabled the automatic backup function!"); // If interval is set to -1 in properties.
        strings.getString("createbudir",    "[Backup] Created folder for backups."); // On first load will need to create the folder specified in properties if not found.
        strings.getString("zipdisabled",    "[Backup] Backup compression is disabled."); // If ZIP'in is disabled.
        
        strings.getString("skipworlds",     "[Backup] Skipping worlds backup, for all worlds."); // Skipping worlds.
        strings.getString("skipplugins",    "[Backup] Skipping plugin backup, for all plugins."); // Skipping plugins.

        strings.getString("newconfigfile",  "[Backup] Your properties.yml was not found, creating default...");
        strings.getString("configoutdated", "[Backup] Your properties.yml file is outdates, please delete it and a new one will be made!");
        
        strings.getString("stoppedlast",    "[Backup] Stopped last backup, start with normal backup cycle!"); // When a player joins after inactivity.

        strings.getString("lastbackup",     "[Backup] Set last backup. It will start in %%ARG%% minutes of server inactivity."); // When last player leaves.
        
        strings.getString("startlast",      "[Backup] Start last backup. When this is done, the server will not run a backup until a player joins the server."); // Start of last backup.
        
        strings.getString("abortedbackup",  "[Backup] Scheduled backup was aborted due to lack of players. Next backup attempt in %%ARG%% minutes."); // No backup required.
        
        strings.getString("disabledworlds", "[Backup] Backup is disabled for the following world(s):"); // Worlds backup is disabled on.

        strings.getString("removeold",      "[Backup] Removing the following backups due to age:"); // Removeold backups message.
        
        strings.getString("errordateformat","[Backup] Error formatting date, bad format string! Formatting date with default format string..."); // Date format error.
        
        strings.getString("errorcreatetemp","[Backup] An error occurs while creating a temporary copy of world %%ARG%%.  Maybe the complete world didnt backup, please take a look at it!"); // Temp file copy error.
        
        strings.getString("backupfailed",   "[Backup] An error occured while backing up. Please report to an admin!"); // Backup failure.
        
        strings.getString("stringnotfound", "String not found - "); // If we couldnt find the string.

        strings.save();
    }
    
    /**
     * Gets a value of the string property.
     * 
     * @param sname The identifier for the string.
     * @return The string from properties, with colors encoded.
     */
    public String getString (String sname) {
        String string = strings.getString(sname);
        if(string != null)
            return colourizeString(string);
        else
            return strings.getString("stringnotfound") + sname;
    }
    
    /**
     * Gets a value of the string property, and replaces options.
     * 
     * @param sname The identifier for the string.
     * @param option The variable to replace %%ARG%% with.
     * @return The string from properties, with colors encoded, and text replaced.
     */
    public String getStringWOPT (String sname, String option) {
        String string = strings.getString(sname);
        if(string != null)
            return colourizeString(string.replaceAll("%%ARG%%", option));
        else
            return strings.getString("stringnotfound") + sname;
    }
    
    /**
     * Encodes the color codes, and returns the encoded string.
     * If the parameter is blank or null, return blank.
     * 
     * @param tocolour The string to encode.
     * @return The encoded string.
     */
    private String colourizeString(String tocolour) {
        if(tocolour != null || tocolour.equals(""))
            return tocolour.replaceAll("&([0-9a-f])", "\u00A7$1");
        else
            return "";
    }
    
}
