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
    private boolean isLastBackup;

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

    /**
     * Run method for reparing the backup.
     */
    @Override
    public void run () {
        
        // Check various parameters.
        handlePrepareBackup();
    }

    
    private void handlePrepareBackup() {
        
        // Continue if this ia a manual backup.
        if(isManualBackup) {
            prepareBackup();
            
        } else {

            // Get variables.
            boolean backupOnlyWithPlayer = settings.getBooleanProperty("backuponlywithplayer");
            int onlineP = server.getOnlinePlayers().length;

            // If we should backup every cycle.
            if (!backupOnlyWithPlayer) {
                prepareBackup();
                
            } else {
                // Backup depending on players.
                if (onlineP == 0) {
                    doNoPlayers();
                } else {
                    prepareBackup();
                }
            }
        }  
    }
    
    /**
     * Called when the scheduled backup is called, but no players are online.
     */
    public void doNoPlayers() {
        
        // Should we stop backups if there are no players?
        if (settings.getBooleanProperty("backuponlywithplayer")) {
            
            // If this should be the last backup.
            if(isLastBackup) {
                LogUtils.sendLog(strings.getString("lastbackup"));
                prepareBackup();
                isLastBackup = false;
            } else {
                LogUtils.sendLog(Level.INFO, strings.getString("abortedbackup", Integer.toString(settings.getIntProperty("backupinterval"))), true);
            }
            
        } else {
            prepareBackup();
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
   
    /**
     * Set the backup as a last backup.
     */
    public void setAsLastBackup () {
        this.isLastBackup = true;
    }
}
