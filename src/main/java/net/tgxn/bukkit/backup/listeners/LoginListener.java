package net.tgxn.bukkit.backup.listeners;

import net.tgxn.bukkit.backup.threading.PrepareBackupTask;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener extends PlayerListener {
    
    private PrepareBackupTask backupTask = null;
    private Plugin plugin;
    
    /**
     * Constructor for Listening for Logins.
     * 
     * @param backupTask The BackupTast to call.
     * @param plugin Plugin to link this class too.
     */
    public LoginListener(PrepareBackupTask backupTask, Plugin plugin) {
        this.backupTask = backupTask;
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        attemptBackup();
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        attemptBackup();
    }
    
    /**
     * Call a last backup.
     */
    private void attemptBackup() {
        backupTask.setAsLastBackup();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
    }

}

