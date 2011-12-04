package net.tgxn.bukkit.backup.threading;

import net.tgxn.bukkit.backup.BackupMain;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.utils.LogUtils;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * This task is running by a syncronized thread from the sheduler. It prepare
 * everything for the BackupTask. It checks, whether it can run a backup now,
 * stop the autosave, make a server wide save of all player, save all world data
 * from the RAM to the disc and collects finnaly all worlds and directories to
 * backup. If this is done, it create an asyncronized thread, the BackupTask.
 * @author Kilian Gaertner
 * @see BackupTask
 */
public class PrepareBackup implements Runnable {

    private final Server server;
    private final Settings settings;
    public Strings strings;
    private boolean isManualBackup;
    private Plugin plugin;
    public boolean isLastBackup;

    public PrepareBackup (Server server, Settings settings, Strings strings) {
        this.server = server;
        this.settings = settings;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = strings;
        isLastBackup = false;
    }

    @Override
    public void run () {
        checkShouldDoBackup();
    }

    /**
     * This method decides whether the backup should be run.
     *
     * It checks:
     * - Online players.
     * - Bypass node.
     * - Manual backup.
     *
     * It then runs the backup if needed.
     */
    private void checkShouldDoBackup() {
        // If it is a manual backup, start it, otherwise, perform checks.
        if(isManualBackup) {
            prepareBackup();
        } else {
            // No player checking.
            if (!settings.getBooleanProperty("backupemptyserver")) {
                prepareBackup();
            } else {
                // Checking online players.
                if(server.getOnlinePlayers().length == 0) {
                    doNoPlayers();
                } else {
                    // Permission checking for bypass node.
                    boolean doBackup = false;
                    if(BackupMain.permissionsHandler != null) {

                        // Get all players.
                        Player[] players = server.getOnlinePlayers();
                        for (int player = 0; player < players.length; player++) {
                            Player currentplayer = players[player];

                            // If any players do not have the node, do the backup.
                            if (!BackupMain.permissionsHandler.has(currentplayer, "backup.bypass")) {
                                doBackup = true;
                            }
                        }
                    } else {
                        doBackup = true;
                    }
                    if(doBackup) {
                        prepareBackup();
                    } else {
                        LogUtils.sendLog("Skipping backup because all players have bypass node.");
                    }
                }
            }
        }  
    }

    /**
     * Prepared for, and starts, a backup.
     */
    protected void prepareBackup() {

        // Notify backup has started.
        notifyStarted();

        // Perform world saving, and turn it off.
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();
        server.dispatchCommand(consoleCommandSender, "save-all");
        server.dispatchCommand(consoleCommandSender, "save-off");

        // Save players.
        server.savePlayers();

        // Send a message advising that zipping is disabled.
        if (!settings.getBooleanProperty("zipbackup"))
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
     * Called when the scheduled backup is called, but no players are online.
     *
     * This checks:
     * - Last backup.
     *
     * If it is the last backup, it starts it, else sends a message.
     */
    public void doNoPlayers() {
        // If this should be the last backup.
        if (isLastBackup) {
            LogUtils.sendLog(strings.getString("lastbackup"));
            prepareBackup();
            isLastBackup = false;
        } else {
            LogUtils.sendLog(Level.INFO, strings.getString("abortedbackup", Integer.toString(settings.getIntProperty("backupinterval"))), true);
        }
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
     * Notify that the backup has started.
     *
     */
    private void notifyStarted() {

        // Inform players backup is about to happen.
        String startBackupMessage = strings.getString("backupstarted");

        if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {

            // Verify Permissions
            if (BackupMain.permissionsHandler != null) {

                // Get all players.
                Player[] players = server.getOnlinePlayers();
                boolean sent = false;
                // Loop through all online players.
                for(int i = 0; i < players.length; i++) {
                    Player currentplayer = players[i];

                    // If the current player has the right permissions, notify them.
                    if(BackupMain.permissionsHandler.has(currentplayer, "backup.notify")) {
                        currentplayer.sendMessage(startBackupMessage);
                        sent = true;
                    }
                }

                if(!sent)
                    if(settings.getBooleanProperty("broardcastmessages"))
                        server.broadcastMessage(startBackupMessage);

            } else {

                // If there are no permissions, notify all.
                if(settings.getBooleanProperty("broardcastmessages"))
                    server.broadcastMessage(startBackupMessage);
            }
        }

        // Send message to log, to be sure.
        LogUtils.sendLog("Started Backup!");
    }


    /**
     * Set the backup as a manual backup. IE: Not scheduled.
     */
    public void setAsManualBackup() {
        this.isManualBackup = true;
    }
   
    /**
     * Set the backup as a last backup.
     */
    public void setAsLastBackup(boolean isLast) {
        this.isLastBackup = isLast;
    }
}
