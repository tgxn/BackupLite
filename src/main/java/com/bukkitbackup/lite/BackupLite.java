package com.bukkitbackup.lite;

import com.bukkitbackup.lite.config.Settings;
import com.bukkitbackup.lite.config.UpdateChecker;
import com.bukkitbackup.lite.events.CommandHandler;
import com.bukkitbackup.lite.events.EventListener;
import com.bukkitbackup.lite.threading.PrepareBackup;
import com.bukkitbackup.lite.utils.LogUtils;
import com.bukkitbackup.lite.utils.SharedUtils;
import java.io.File;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BackupLite extends JavaPlugin {

    public File mainDataFolder;
    public int mainBackupTaskID = -2;
    public int saveAllTaskID = -2;
    
    private static Settings settings;
    private PrepareBackup prepareBackup;

    @Override
    public void onLoad() {

        mainDataFolder = this.getDataFolder();

        LogUtils.initLogUtils(this);

        SharedUtils.checkFolderAndCreate(mainDataFolder);

        settings = new Settings(this, new File(mainDataFolder, "config.yml"));

    }

    @Override
    public void onEnable() {

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(pluginServer, settings);

        // Initalize the update checker code.
        pluginServer.getScheduler().scheduleAsyncDelayedTask(this, new UpdateChecker(this));

        // Initalize Command Listener.
        getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings));
        getCommand("bu").setExecutor(new CommandHandler(prepareBackup, this, settings));

        // Initalize Event Listener.
        EventListener eventListener = new EventListener(prepareBackup, this, settings);
        pluginManager.registerEvents(eventListener, this);

        // Configure main backup task schedule.
        int backupInterval = settings.getIntervalInMinutes("backupinterval");
        if (backupInterval != -1 && backupInterval != 0) {

            // Convert to server ticks.
            int backupIntervalInTicks = (backupInterval * 1200);


            mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupIntervalInTicks, backupIntervalInTicks);
            
        } else {
            LogUtils.sendLog("Backup schedule is disabled.");
        }

        // Notify loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }

    @Override
    public void onDisable() {

        // Stop and scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);

        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completely un-loaded!", false);
    }
}
