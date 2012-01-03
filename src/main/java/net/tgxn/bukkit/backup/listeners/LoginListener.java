package net.tgxn.bukkit.backup.listeners;

import net.tgxn.bukkit.backup.BackupMain;
import net.tgxn.bukkit.backup.config.*;
import net.tgxn.bukkit.backup.utils.*;
import net.tgxn.bukkit.backup.threading.PrepareBackup;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for login events, and perform actions based on what happened.
 *
 * Updated 16.11.11
 * = Added Login Listener to cancel scheduled backup.
 *
 * @author gamerx
 */
public class LoginListener extends PlayerListener {
    
    private PrepareBackup prepareBackup = null;
    private Plugin plugin;
    private Settings settings;
    private Strings strings;
    private int lastBackupID;
    
    /**
     * Constructor for listening for login events.
     * 
     * @param backupTask The BackupTast to call.
     * @param plugin Plugin to link this class too.
     */
    public LoginListener(PrepareBackup backupTask, Plugin plugin, Settings settings, Strings strings) {
        this.prepareBackup = backupTask;
        this.plugin = plugin;
        this.settings = settings;
        this.strings = strings;
        lastBackupID = -2;
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerPart();
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        playerPart();
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerJoin();
    }

    /**
     * Called when a player leaves the server.
     *
     */
    private void playerPart() {
         int onlinePlayers = plugin.getServer().getOnlinePlayers().length;
         // Check if it was the last player.
         if (onlinePlayers == 1) {
            prepareBackup.setAsLastBackup(true);
            //plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
            int intervalInMinutes = settings.getIntervalInMinutes();
            if (intervalInMinutes != -1) {
                int interval =  intervalInMinutes * 1200;
                lastBackupID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, prepareBackup, interval, interval);
                LogUtils.sendLog("Scheduled last backup for " + intervalInMinutes +" minutes;");
            } else {
                LogUtils.sendLog(strings.getString("disbaledauto"));
            }
         }
    }

    /**
     * Called when a player joins the server.
     *
     */
    private void playerJoin() {
        if(prepareBackup.isLastBackup || lastBackupID != -2) {
            plugin.getServer().getScheduler().cancelTask(lastBackupID);
            lastBackupID = -2;
            prepareBackup.setAsLastBackup(false);
            LogUtils.sendLog("Stopped last backup, because someone joined.");
        }
    }
}

