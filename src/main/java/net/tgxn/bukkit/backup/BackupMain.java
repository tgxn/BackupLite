package net.tgxn.bukkit.backup;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import net.tgxn.bukkit.backup.utils.*;
import net.tgxn.bukkit.backup.config.*;
import net.tgxn.bukkit.backup.listeners.*;
import net.tgxn.bukkit.backup.threading.PrepareBackup;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * The plugin's main class.
 *
 * @author gamerx
 */
public class BackupMain extends JavaPlugin {
    
    public static PermissionHandler permissionsHandler;
    public int mainBackupTaskID = -2;
    
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    
    @Override
    public void onLoad() {
        
        // Initalize utilities.
        DebugUtils.initDebugUtils(this);
        LogUtils.initLogUtils(this);
        
        // Perform datafile check.
        checkFolderAndCreate(this.getDataFolder());
        
        // Load plugin's string settings.
        strings = new Strings(this);
        
        // Load plugin's main configuration.
        File configFile = new File(this.getDataFolder(), "config.yml");
        settings = new Settings(this, configFile, strings);

        // Initalize log file.
        LogUtils.finishInitLogUtils(settings.getStringProperty("backuplogname"), settings.getBooleanProperty("displaylog"));

        // Do folder checking for backups folder.
        if(checkFolderAndCreate(new File(settings.getStringProperty("backuppath"))))
            LogUtils.sendLog(strings.getString("createbudir"));
    }
    
    @Override
    public void onEnable() {
        
        // Set up permissions for plugin.
        initPermissions();

        // Get server and player managers.
        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();

        // Create new backup instance.
        prepareBackup = new PrepareBackup(server, settings, strings);

        // Initalize plugin listeners.
        getCommand("backup").setExecutor(new CommandListener(prepareBackup, this, settings, strings));
        LoginListener loginListener = new LoginListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_KICK, loginListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_JOIN, loginListener, Priority.Normal, this);

        // Schedule timer, checks if there is a timer and enables task.
        int backupInterval = settings.getIntProperty("backupinterval");
        if (backupInterval != -1) {
            backupInterval *= 1200;
            mainBackupTaskID = server.getScheduler().scheduleSyncRepeatingTask(this, prepareBackup, backupInterval, backupInterval);
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }
        
        // Loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }
    
    @Override
    public void onDisable () {
        
        // Stop and scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed un-loading!", false);
    }
    
    /**
     * Check if the Permissions System is available, and setup the handler.
     */
    private void initPermissions() {

        // Check if not already initalized.
        if (permissionsHandler != null)
            return;
                
        // Get permissions plugin.
        Plugin testPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
        
        // If we were able to get the permissions plugin.
        if (testPermissions != null) {
            permissionsHandler = ((Permissions) testPermissions).getHandler();
            LogUtils.sendLog(strings.getString("hookedperms"));
        } else {
            LogUtils.sendLog(strings.getString("defaultperms"));
        }
    }
    
    /**
     * Checks if a folder exists and creates it if it does not.
     * 
     * @param toCheck File to check.
     * @return True if created, false if exists.
     */
    private boolean checkFolderAndCreate(File toCheck) {
        if (!toCheck.exists()) {
            try {
                if (toCheck.mkdirs()) {
                    return true;
                }
            } catch (SecurityException se) {
                DebugUtils.debugLog(se.getStackTrace());
            }
        }
        return false;
    }
}
