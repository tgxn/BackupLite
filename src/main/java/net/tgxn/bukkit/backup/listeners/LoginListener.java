package net.tgxn.bukkit.backup.listeners;

import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.threading.PrepareBackup;

import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener extends PlayerListener {
    
    private PrepareBackup prepareBackup = null;
    private Plugin plugin;
    private Settings settings;
    private Strings strings;
    private int lastBackupID;
    
    /**
     * Constructor for Listening for Logins.
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
     * Call a last backup.
     */
    private void playerPart() {

         int amntPlayersOnline = plugin.getServer().getOnlinePlayers().length;
         if (amntPlayersOnline != 1) {
             LogUtils.sendLog("was not last player to leave.");
             return;
         }

        prepareBackup.setAsLastBackup(true);
        //plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
        int intervalold = settings.getIntProperty("backupinterval");
        if (intervalold != -1) {
            int interval =  intervalold * 1200;
            lastBackupID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, prepareBackup, interval, interval);
            LogUtils.sendLog("Scheduled last backup for " + intervalold +" minutes;");
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }
    }

    private void playerJoin() {
        if(prepareBackup.isLastBackup || lastBackupID != -2) {
            plugin.getServer().getScheduler().cancelTask(lastBackupID);
            lastBackupID = -2;
            prepareBackup.setAsLastBackup(false);
            LogUtils.sendLog("Stopped last backup, because someone joined.");
        }
    }

}

