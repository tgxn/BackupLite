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

package net.tgxn.bukkit.backup.threading;

import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import net.tgxn.bukkit.backup.BackupMain;
import org.bukkit.entity.Player;

/**
 * This task is running by a syncronized thread from the sheduler. It prepare
 * everything for the BackupTask. It checks, whether it can run a backup now,
 * stop the autosave, make a server wide save of all player, save all world data
 * from the RAM to the disc and collects finnaly all worlds and directories to
 * backup. If this is done, it create an asyncronized thread, the BackupTask.
 * @author Kilian Gaertner
 * @see BackupTask
 */
public class PrepareBackupTask implements Runnable {

    private final Server server;
    private final Settings settings;
    public Strings strings;
    private boolean isManualBackup;
    private Plugin plugin;

    /**
     * The only constructor for the BackupTask.
     * @param server The server where the Task is running on
     * @param settings This must be a loaded PropertiesSystem
     */
    public PrepareBackupTask (Server server, Settings settings, Strings strings) {
        this.server = server;
        this.settings = settings;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = strings;
    }

    @Override
    public void run () {

        // Check if we should be doing backup.
        boolean backupOnlyWithPlayer = settings.getBooleanProperty("backuponlywithplayer");
        if ((backupOnlyWithPlayer && server.getOnlinePlayers().length > 0) || !backupOnlyWithPlayer || isManualBackup) {
            prepareBackup();
        } else {
            LogUtils.sendLog(Level.INFO, strings.getStringWOPT("abortedbackup", Integer.toString(settings.getIntProperty("backupinterval") / 1200)), true);
        }
    }

    protected void prepareBackup() {
        
        // Inform players backup is about to happen.
        String startBackupMessage = strings.getString("backupstarted");
        
        if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {
            
            // Verify Permissions
            if (BackupMain.Permissions != null) {
                
                // Get all players.
                Player[] players = server.getOnlinePlayers();
                
                // Loop through all online players.
                for(int i = 0; i < players.length; i++) {
                    Player currentplayer = players[i];
                    
                    // If the current player has the right permissions, notify them.
                    if(BackupMain.Permissions.has(currentplayer, "backup.notify"))
                        currentplayer.sendMessage(startBackupMessage);
                }
                
                // Send message to log, to be sure.
                LogUtils.sendLog(startBackupMessage);
                 
            } else {
                
                // If there are no permissions, notify all.
                server.broadcastMessage(startBackupMessage);
            }
        }

        // Save to file, and then turn saving off.
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();
        server.dispatchCommand(consoleCommandSender, "save-all");
        server.dispatchCommand(consoleCommandSender, "save-off");

        // Save players current values.
        server.savePlayers();

        // Determine if backups should be ZIP'd.
        boolean hasToZIP = settings.getBooleanProperty("zipbackup");

        // Send a message advising that it is disabled.
        if (!hasToZIP)
            LogUtils.sendLog(strings.getString("zipdisabled"));

        // Create list of worlds to ignore.
        List<String> ignoredWorldNames = getIgnoredWorldNames();
        LinkedList<String> worldsToBackup = new LinkedList<String>();
        for (World world : server.getWorlds()) {
            if ((world.getName() != null) && !world.getName().isEmpty() && (!ignoredWorldNames.contains(world.getName()))) {
                LogUtils.sendLog(Level.FINE, "Adding world '" + world.getName() + "' to backup list", true);
                worldsToBackup.add(world.getName());
            }
        }
        
        // Scedule the backup.
        server.getScheduler().scheduleAsyncDelayedTask(plugin, new BackupTask(settings, strings, worldsToBackup, server));
        isManualBackup = false;
    }
    
    /**
     * Function to get world names to ignore.
     * 
     * @return A List[] of the world names we should not be backing up.
     */
    private List<String> getIgnoredWorldNames() {
        
        // Get skipped worlds form config.
        List<String> worldNames = Arrays.asList(settings.getStringProperty("skipworlds").split(";"));
        
        // Loop all ignored worlds.
        if (worldNames.size() > 0 && !worldNames.get(0).isEmpty()) {
            
            // Log what worlds are disabled.
            LogUtils.sendLog(strings.getString("disabledworlds"));
            LogUtils.sendLog(worldNames.toString());
        }
        
        // Return the world names.
        return worldNames;
    }
    
    /**
     * Set the backup as a manual backup. IE: Not scheduled.
     */
    public void setAsManualBackup () {
        this.isManualBackup = true;
    }
}
