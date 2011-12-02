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
 * Main Backup Plugin.
 * - Handles all backing up, and scheduling.
 *
 * Updated 16.11.11
 * = Updated context on variables.
 * = Fixed some documentation.
 *
 * @author gamerx
 */
public class BackupMain extends JavaPlugin {
    
    public static PermissionHandler permissionsHandler;
    public int mainBackupTaskID = -2;
    
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    
    /**
     * onLoad method, Called after a plugin is loaded but before it has been enabled..
     */
    @Override
    public void onLoad() {
         
        // Initalize Utilities.
        DebugUtils.initDebugUtils(this);
        LogUtils.initLogUtils(this);
        
        // Perform DataFile check.
        checkFolder(this.getDataFolder());
        
        // Load Strings.
        strings = new Strings(this);
        
        // Load Settings.
        File configFile = new File(this.getDataFolder(), "config.yml");
        settings = new Settings(this, configFile, strings);

        // Use settings in log utils.
        LogUtils.finishInitLogUtils(settings.getStringProperty("backuplogname"), settings.getBooleanProperty("displaylog"));

        // Perform backup folder check.
        if(checkFolder(new File(settings.getStringProperty("backuppath"))))
            LogUtils.sendLog(strings.getString("createbudir"));
    }
    //Small Chnage to initiate build testing.
    /**
     * onEnable method, Called when this plugin is enabled.
     */
    @Override
    public void onEnable() {
        
        // CHeck for Permissions system.
        setupPermissions();

        // Get system objects.
        Server server = getServer();
        PluginManager pm = server.getPluginManager();

        // Setup the scheduled BackupTask.
        prepareBackup = new PrepareBackup(server, settings, strings);

        // Setup listeners.
        getCommand("backup").setExecutor(new CommandListener(prepareBackup, this, settings, strings));
        LoginListener loginListener = new LoginListener(prepareBackup, this, settings, strings);
        pm.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_KICK, loginListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, loginListener, Priority.Normal, this);

        // Setup the scheduled backuptask, or turn it off if not needed.
        int interval = settings.getIntProperty("backupinterval");
        if (interval != -1) {
            interval *= 1200;
            mainBackupTaskID = server.getScheduler().scheduleSyncRepeatingTask(this, prepareBackup, interval, interval);
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }
        
        // Inform Startup Complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }
    
    /**
     * onDisable method, Called when this plugin is disabled.
     */
    @Override
    public void onDisable () {
        
        // Cancell any scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Inform shutdown successfull.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed un-loading!", false);
    }
    
    /**
     * Check if the Permissions System is available, and setup the handler.
     */
    private void setupPermissions() {

        // Make sure that it hasnt already been loaded.
        if (permissionsHandler != null)
            return;
                
        // Get permissions plugin.
        Plugin testPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
        
        // If we were able to get the Permissions plugin.
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
    private boolean checkFolder(File toCheck) {
        // If it does not exist.
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
