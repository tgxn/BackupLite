/*
 *  Copyright (C) 2011 Domenic Horner
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
import java.io.File;

public class Strings {

    public Configuration strings;
    
    public Strings (Plugin plugin) {
        strings = new Configuration(new File(plugin.getDataFolder(),  "strings.yml"));
        try{
            strings.load();
	} catch(Exception ex){
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
    
    public String getString (String sname) {
        String string = strings.getString(sname);
        if(string != null)
            return colourizeString(string);
        else
            return strings.getString("stringnotfound") + sname;
    }
    
    public String getStringWOPT (String sname, String option) {
        String string = strings.getString(sname);
        if(string != null)
            return colourizeString(string.replaceAll("%%ARG%%", option));
        else
            return strings.getString("stringnotfound") + sname;
    }
    
    private String colourizeString(String tocolour) {
        if(tocolour != null)
            return tocolour.replaceAll("&([0-9a-f])", "\u00A7$1");
        else
            return "";
    }
    
}
