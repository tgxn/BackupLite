package net.tgxn.bukkit.backup;

import java.io.File;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.listeners.CommandListener;
import net.tgxn.bukkit.backup.listeners.LoginListener;
import net.tgxn.bukkit.backup.threading.PrepareBackup;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.SharedUtils;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class file for Backup.
 *
 * @author Domenic Horner (gamerx)
 */
public class BackupMain extends JavaPlugin {
    
    //public static PermissionHandler permissionsHandler;
    public int mainBackupTaskID = -2;
    public File mainDataFolder;
    
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    
    @Override
    public void onLoad() {
        
        // Initalize main data folder.
        mainDataFolder = this.getDataFolder();
        
        // Initalize log utilities.
        LogUtils.initLogUtils(this);
        
        // Perform datafile check.
        SharedUtils.checkFolderAndCreate(mainDataFolder);
        
        // Load plugin configuration.
        strings = new Strings(new File(mainDataFolder, "strings.yml"));
        settings = new Settings(this, new File(mainDataFolder, "config.yml"), strings);
        
        strings.checkStringsVersion(settings.getStringProperty("requiredstrings"));
        
        // Finish init of LogUtils.
        LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog"), settings.getBooleanProperty("logtofile"), settings.getStringProperty("backuplogname"));
        
        // Do folder checking for backups folder.
        if(SharedUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath"))))
            LogUtils.sendLog(strings.getString("createbudir"));
    }
    
    @Override
    public void onEnable() {
        
        // Initalize permissions handler.
        //initPermissions();

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(pluginServer, settings, strings);

        // Initalize plugin listeners.
        getCommand("backup").setExecutor(new CommandListener(prepareBackup, this, settings, strings));
        LoginListener loginListener = new LoginListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_KICK, loginListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_JOIN, loginListener, Priority.Normal, this);

        // Schedule timer, checks if there is a timer and enables task.
        int backupInterval = settings.getIntervalInMinutes();
        if (backupInterval != 0) {
            backupInterval *= 1200;
            mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupInterval, backupInterval);
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }
        
        String interval = settings.getStringProperty("backupinterval");
        String max = Integer.toString(settings.getIntProperty("maxbackups"));
        String empty = (settings.getBooleanProperty("backupemptyserver")) ? "Yes" : "No";
        String everything = (settings.getBooleanProperty("backupeverything")) ? "Yes" : "No";
        String split = (settings.getBooleanProperty("splitbackup")) ? "Yes" : "No";
        String zip = (settings.getBooleanProperty("zipbackup")) ? "Yes" : "No";
        String path = settings.getStringProperty("backuppath");
        
        if(settings.getBooleanProperty("showconfigonstartup")) {
            LogUtils.sendLog("Configuration:");
            LogUtils.sendLog("Interval: "+interval+", Max: "+max+", On Empty: "+empty+", Everything: "+everything+".", false);
            LogUtils.sendLog("Split: "+split+", ZIP: "+zip+", Path: "+path+".", false);
        }
        // Loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }
    
    @Override
    public void onDisable () {
        
        //this.getServer().getScheduler().getPendingTasks().
        
        // Stop and scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed un-loading!", false);
    }
    
    /**
     * Check if the Permissions System is available, and setup the handler.
     */
//    private void initPermissions() {
//
//        // Check if not already initalized.
//        if (permissionsHandler != null)
//            return;
//                
//        // Get permissions plugin.
//        Plugin testPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
//        
//        // If we were able to get the permissions plugin.
//        if (testPermissions != null) {
//            permissionsHandler = ((Permissions) testPermissions).getHandler();
//            LogUtils.sendLog(strings.getString("hookedperms"));
//        } else {
//            LogUtils.sendLog(strings.getString("defaultperms"));
//        }
//    }
}
