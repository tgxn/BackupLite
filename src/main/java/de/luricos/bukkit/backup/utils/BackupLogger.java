/*
 * Backup - CraftBukkit server Backup plugin (continued)
 * Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
 * Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
 *
 * Wormhole X-Treme Worlds Plugin for Bukkit
 * Copyright (C) 2011 Lycano <https://github.com/lycano/Wormhole-X-Treme/>
 *
 * Wormhole X-Treme Worlds Plugin for Bukkit
 * Copyright (C) 2011 Dean Bailey <https://github.com/alron/Wormhole-X-Treme-Worlds>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.luricos.bukkit.backup.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lycano
 */
public class BackupLogger {
    private static Level logLevel = Level.INFO;
    private static Logger logger = null;
    private static String logPluginName = null;
    private static String logPluginVersion = null;
    
    public static void initLogger(String pluginName, String pluginVersion, Level logLevel) {
        if (BackupLogger.logger == null) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                BackupLogger.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + pluginName);
            }
            
            BackupLogger.logLevel = logLevel;
            BackupLogger.logger.setLevel(logLevel);
            BackupLogger.logPluginName = pluginName;
            BackupLogger.logPluginVersion = pluginVersion;
        }
    }
    
    public static void setLogLevel(Level logLevel) {
        BackupLogger.logLevel = logLevel;
        BackupLogger.logger.setLevel(logLevel);
    }

    public static void prettyLog(String message) {
        prettyLog(Level.INFO, false, message);
    }

    public static void prettyLog(boolean version, String message) {
        prettyLog(Level.INFO, version, message);
    }

    public static void prettyLog(final Level logLevel, final boolean version, final String message) {
        final String prettyName = ("[" + getName() + "]");
        final String prettyVersion = ("[v" + getVersion() + "]");
        String prettyLogLine = prettyName;
        if (version) {
            prettyLogLine += prettyVersion;
        }
        
        logger.log(logLevel, prettyLogLine + " " + message);
    }
    
    public static Level getLogLevel() {
        return logLevel;
    }
    
    public static String getVersion() {
        return logPluginVersion;
    }
    
    public static String getName() {
        return logPluginName;
    }
}