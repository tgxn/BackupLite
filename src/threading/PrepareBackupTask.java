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

package threading;

import backup.Properties;
import backup.Strings;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

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
    private final Properties properties;
    public Strings strings;
    private String backupName;
    private boolean isManualBackup;
    private Plugin plugin;

    /**
     * The only constructor for the BackupTask.
     * @param server The server where the Task is running on
     * @param properties This must be a loaded PropertiesSystem
     */
    public PrepareBackupTask (Server server, Properties properties) {
        this.server = server;
        this.properties = properties;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = new Strings(plugin);
    }

    @Override
    public void run () {
        
        // Check if we should be doing backup
        boolean backupOnlyWithPlayer = properties.getBooleanProp("backuponlywithplayer");
        if ((backupOnlyWithPlayer && server.getOnlinePlayers().length > 0) || !backupOnlyWithPlayer || isManualBackup || backupName != null)
            prepareBackup();
        else
            System.out.println(strings.getStringWOPT("abortedbackup", Integer.toString(properties.getIntProp("backupinterval") / 1200)));
    }

    protected void prepareBackup() {

        // Inform players backup is about to happen.
        String startBackupMessage = strings.getString("backupstarted");
        if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {
            server.broadcastMessage(startBackupMessage);
        }
        
        ConsoleCommandSender ccs;
        // Compatible with dev build (more than 857)
            //ccs = server.getConsoleSender();
            
        // Compatable with RB. (less than 857)
            ccs = new ConsoleCommandSender(server);
            
        // Save to file, and then turn saving off.
        server.dispatchCommand(ccs, "save-all");
        server.dispatchCommand(ccs, "save-off");

        // Save players current values.
        server.savePlayers();

        // Get list of worlds to ignore.
        String[] ignoredWorlds = getToIgnoreWorlds();
        LinkedList<String> worldsToBackup = new LinkedList<String>();

        // Determine if backups should be ZIP'd.
        boolean hasToZIP = properties.getBooleanProp("zipbackup");
        
        // Send a message advising that it is disabled.
        if (!hasToZIP)
            System.out.println(strings.getString("zipdisabled"));

        // Iterate through all possible worlds, and add them to queue.
        outer:
        for (World world : server.getWorlds()) {
            String worldName = world.getName();
            for (String ignoredWorldName : ignoredWorlds) {
                if (ignoredWorldName.equalsIgnoreCase(worldName))
                    continue outer;
            }
            worldsToBackup.add(worldName);
            world.save();
        }
        
        server.getScheduler().scheduleAsyncDelayedTask(plugin, new BackupTask(properties, worldsToBackup, server, backupName));
        backupName = null;
        isManualBackup = false;
    }

    private String[] getToIgnoreWorlds () {
        String[] worldNames = properties.getStringProp("skipworlds").split(";");
        if (worldNames.length > 0 && !worldNames[0].isEmpty()) {
            
            // Log what worlds are disabled.
            System.out.println(strings.getString("disabledworlds"));
            System.out.println(Arrays.toString(worldNames));
        }
        return worldNames;
    }
    

    public void setBackupName (String backupName) {
        this.backupName = backupName;
    }

    public void setAsManualBackup () {
        this.isManualBackup = true;
    }
}
