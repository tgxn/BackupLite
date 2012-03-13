package net.tgxn.bukkit.backup;

import java.io.File;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.events.CommandHandler;
import net.tgxn.bukkit.backup.events.EventListener;
import net.tgxn.bukkit.backup.threading.PrepareBackup;
import net.tgxn.bukkit.backup.utils.CheckInUtil;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.SharedUtils;
import net.tgxn.bukkit.backup.utils.SyncSaveAllUtil;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BackupMain extends JavaPlugin {

    public int mainBackupTaskID = -2;
    public int saveAllTaskID = -2;
    public File mainDataFolder;
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    private SyncSaveAllUtil syncSaveAllUtil;

    @Override
    public void onLoad() {

        // Initalize main data folder variable.
        mainDataFolder = this.getDataFolder();

        // Initalize logging utilities.
        LogUtils.initLogUtils(this);

        // check and create main datafile.
        SharedUtils.checkFolderAndCreate(mainDataFolder);

        // Load configuration files.
        strings = new Strings(new File(mainDataFolder, "strings.yml"));
        settings = new Settings(this, new File(mainDataFolder, "config.yml"), strings);

        // Run version checking on strings file.
        strings.checkStringsVersion(settings.getStringProperty("requiredstrings"));

        // Complee initalization of LogUtils.
        LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog"));

        // Check backup path.
        if (SharedUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath")))) {
            LogUtils.sendLog(strings.getString("createbudir"));
        }

    }

    @Override
    public void onEnable() {

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(pluginServer, settings, strings);

        // Initalize Command Listener.
        getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings, strings));

        // Initalize Event Listener.
        EventListener eventListener = new EventListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvents(eventListener, this);

        // Configure main backup task schedule.
        int backupInterval = settings.getIntervalInMinutes("backupinterval");
        if (backupInterval != -1 && backupInterval != 0) {

            // Convert to server ticks.
            int backupIntervalInTicks = backupInterval *= 1200;

            // Should the schedule repeat?
            if (settings.getBooleanProperty("norepeat")) {
                mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncDelayedTask(this, prepareBackup, backupIntervalInTicks);
                LogUtils.sendLog(strings.getString("norepeatenabled", Integer.toString(backupInterval)));
            } else {
                mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupIntervalInTicks, backupIntervalInTicks);
            }
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }

        // Configure save-all schedule.
        int saveAllInterval = settings.getIntervalInMinutes("saveallinterval");
        if (saveAllInterval != 0 && saveAllInterval != -1) {

            // Convert to server ticks.
            int saveAllIntervalInTicks = saveAllInterval *= 1200;

            LogUtils.sendLog(strings.getString("savealltimeron", Integer.toString(saveAllInterval)));

            // Syncronised save-all.
            syncSaveAllUtil = new SyncSaveAllUtil(pluginServer, 0);
            saveAllTaskID = pluginServer.getScheduler().scheduleSyncRepeatingTask(this, syncSaveAllUtil, saveAllIntervalInTicks, saveAllIntervalInTicks);
        }

        // Version checking task.
        if (settings.getBooleanProperty("enableversioncheck")) {
            pluginServer.getScheduler().scheduleAsyncDelayedTask(this, new CheckInUtil(this.getDescription().getVersion(), strings));
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
