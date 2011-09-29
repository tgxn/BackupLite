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

package net.tgnx.bukkit.backup.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class Strings {
    private Configuration strings;

    public Strings(Plugin plugin) {
        strings = new Configuration(new File(plugin.getDataFolder(), "strings.yml"));
        try {
            strings.load();
        } catch (Exception ex) {
            /** @TODO create exception classes **/
            ex.printStackTrace(System.out);
        }

        // In-Game Messages
        /** Backup started **/
        strings.getString("backupstarted", "Started Backup...");
        /** Backup complete **/
        strings.getString("backupfinished", "Finished Backup!");
        /** Player does not have permission **/
        strings.getString("norights", "You do not have the rights to run this backup!");

        // Console-Messages
        /** onLoad permission check **/
        strings.getString("defaultperms", "Permissions system not detected, defaulting to OP.");
        /** Permissions-Plugin found **/
        strings.getString("hookedperms", "Found and hooked permissions plugin.");
        /** Interval set to -1 in properties **/
        strings.getString("disbaledauto", "You have disabled the automatic backup function!");
        /** First load create folder message **/
        strings.getString("createbudir", "Created folder for backups.");
        /** ZIP'in is disabled **/
        strings.getString("zipdisabled", "Backup compression is disabled.");
        /** Skipping worlds **/
        strings.getString("skipworlds", "Skipping worlds backup, for all worlds.");
        /** Skipping plugin's folder backup **/
        strings.getString("skipplugins", "Skipping plugin backup, for all plugins.");
        /** Player joins after inactivity **/
        strings.getString("stoppedlast", "Stopped last backup, start with normal backup cycle!");
        /** Last Player has left **/
        strings.getString("lastbackup", "Set last backup. It will start in %%ARG%% minutes of server inactivity.");
        /** Start last backup **/
        strings.getString("startlast", "Start last backup. When this is done, the server will not run a backup until a player joins the server.");
        /** No backup required **/
        strings.getString("abortedbackup", "Scheduled backup was aborted due to lack of players. Next backup attempt in %%ARG%% minutes.");
        /** Backup is disabled for [worlds] **/
        strings.getString("disabledworlds", "Backup is disabled for the following world(s):");
        /** Remove old backups message **/
        strings.getString("removeold", "Removing the following backups due to age:");
        /** Date format error **/
        strings.getString("errordateformat", "Error formatting date, bad format string! Formatting date with default format string...");
        /** Temp file copy error **/
        strings.getString("errorcreatetemp", "An error occurs while creating a temporary copy of world %%ARG%%.  Maybe the complete world didnt backup, please take a look at it!");
        /** Backup failure **/
        strings.getString("backupfailed", "An error occured while backing up. Please report to an admin!");
        /** Requested property not found **/
        strings.getString("stringnotfound", "String not found - ");

        strings.save();
    }

    public String getString(String property) {
        String string = strings.getString(property);
        if (string != null)
            return colorizeString(string);
        else
            return strings.getString("stringnotfound") + property;
    }

    public String getStringWOPT(String property, String option) {
        String string = strings.getString(property);
        if (string != null)
            return colorizeString(string.replaceAll("%%ARG%%", option));
        else
            return strings.getString("stringnotfound") + property;
    }

    private String colorizeString(String toColor) {
        if (toColor != null)
            return toColor.replaceAll("&([0-9a-f])", "\u00A7$1");
        else
            return "";
    }

}
