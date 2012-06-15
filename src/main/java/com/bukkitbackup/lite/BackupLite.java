package com.bukkitbackup.lite;

import com.bukkitbackup.lite.config.Settings;
import com.bukkitbackup.lite.config.UpdateChecker;
import com.bukkitbackup.lite.events.CommandHandler;
import com.bukkitbackup.lite.events.EventListener;
import com.bukkitbackup.lite.threading.PrepareBackup;
import com.bukkitbackup.lite.utils.FileUtils;
import com.bukkitbackup.lite.utils.LogUtils;
import com.bukkitbackup.lite.utils.MetricUtils;
import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public class BackupLite extends JavaPlugin {

    public File pluginDataFolder;
    public int backupTaskID = -2;
    private String clientID;
    private static Settings settings;
    private PrepareBackup prepareBackup;

    @Override
    public void onLoad() {
        pluginDataFolder = this.getDataFolder();
        LogUtils.initLogUtils(this);
        FileUtils.checkFolderAndCreate(pluginDataFolder);
        settings = new Settings(new File(pluginDataFolder, "config.yml"));
        try {
            MetricUtils metricUtils = new MetricUtils(this);
            metricUtils.start();
            clientID = metricUtils.guid;
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex, "Exception loading metrics.");
        }
        this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new UpdateChecker(this.getDescription(), clientID));
    }

    @Override
    public void onEnable() {
        prepareBackup = new PrepareBackup(getServer(), settings);

        getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings));
        getCommand("bu").setExecutor(new CommandHandler(prepareBackup, this, settings));

        EventListener eventListener = new EventListener(prepareBackup, this, settings);
        getServer().getPluginManager().registerEvents(eventListener, this);

        int backupInterval = settings.getIntervalInMinutes("backupinterval");
        if (backupInterval != -1 && backupInterval != 0) {
            int backupIntervalInTicks = (backupInterval * 1200);
            backupTaskID = getServer().getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupIntervalInTicks, backupIntervalInTicks);
        } else {
            LogUtils.sendLog("Backup schedule is disabled.");
        }

        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!");
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        LogUtils.sendLog(this.getDescription().getFullName() + " has completely un-loaded!");
    }
}
