package com.bukkitbackup.plugin.events;

import com.bukkitbackup.plugin.config.Settings;
import com.bukkitbackup.plugin.config.Strings;
import com.bukkitbackup.plugin.threading.PrepareBackup;
import com.bukkitbackup.plugin.utils.LogUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listens for login events, and perform actions based on what happened.
 *
 * @author Domenic Horner (gamerx)
 */
public class EventListener implements Listener {
    
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
    public EventListener(PrepareBackup backupTask, Plugin plugin, Settings settings, Strings strings) {
        this.prepareBackup = backupTask;
        this.plugin = plugin;
        this.settings = settings;
        this.strings = strings;
        lastBackupID = -2;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerPart();
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKick(PlayerKickEvent event) {
        playerPart();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerJoin();
    }

    /**
     * Called when a player leaves the server.
     *
     */
    private void playerPart() {
         int onlinePlayers = plugin.getServer().getOnlinePlayers().length;
         // Check if it was the last player, and we need to stop backups after this last player leaves.
         if (onlinePlayers == 1 && settings.getBooleanProperty("backupemptyserver")) {
            prepareBackup.setAsLastBackup(true);
            int intervalInMinutes = settings.getIntervalInMinutes("backupinterval");
            if (intervalInMinutes != 0) {
                int interval =  intervalInMinutes * 1200;
                lastBackupID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, prepareBackup, interval);
                LogUtils.sendLog("Scheduled last backup for " + intervalInMinutes +" minutes.");
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
        if(lastBackupID != -2) {
            plugin.getServer().getScheduler().cancelTask(lastBackupID);
            lastBackupID = -2;
            prepareBackup.setAsLastBackup(false);
            LogUtils.sendLog("Stopped last backup, because someone joined.");
        }
    }
}

