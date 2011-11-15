package net.tgxn.bukkit.backup;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.listeners.CommandListener;
import net.tgxn.bukkit.backup.listeners.LoginListener;
import net.tgxn.bukkit.backup.threading.PrepareBackup;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.DebugUtils;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BackupMain extends JavaPlugin {
    
    public static PermissionHandler Permissions;
    public int mainBackupTaskID = -2;
    
    protected static Strings strings;
    protected static Settings settings;
    private PrepareBackup preparedBackupTask;
    
    /**
     * onLoad method, Called after a plugin is loaded but before it has been enabled..
     */
    @Override
    public void onLoad() {
         
        // Init DebugUtils, for all things buggy.
        DebugUtils.initDebugUtils(this);
        
        // Init LogUtils, for logging purposes.
        LogUtils.initLogUtils(this);
        
        // Check the plugin's data folder exists, create if needed.
        checkFolder(this.getDataFolder());
        
        // Load Strings, create if needed.
        strings = new Strings(this);
        
        // Load Properties, create if needed.
        File configFile = new File(this.getDataFolder(), "config.yml");
        settings = new Settings(this, configFile, strings);
        
        // Check for the backup folder.
        if(checkFolder(new File(settings.getStringProperty("backuppath"))))
            LogUtils.sendLog(strings.getString("createbudir"));
        
    }
    
    /**
     * onEnable method, Called when this plugin is enabled.
     */
    @Override
    public void onEnable () {
        
        // Check and load permissions system.
        setupPermissions();

        // Get server object.
        Server server = getServer();

        // Get PluginManager object.
        PluginManager pm = server.getPluginManager();

        // Setup the scheduled BackupTask.
        preparedBackupTask = new PrepareBackup(server, settings, strings);

        // Setup the CommandListener, for commands.
        getCommand("backup").setExecutor(new CommandListener(preparedBackupTask, settings, strings, this));

        // Setup loginlistener.
        LoginListener loginListener = new LoginListener(preparedBackupTask, this, settings, strings);
        pm.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_KICK, loginListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, loginListener, Priority.Normal, this);

        // Setup the scheduled backuptask, or turn it off if not needed.
        int interval = settings.getIntProperty("backupinterval");
        if (interval != -1) {
            interval *= 1200;
            mainBackupTaskID = server.getScheduler().scheduleSyncRepeatingTask(this, preparedBackupTask, interval, interval);
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
    private void setupPermissions () {

        // Make sure that it hasnt already been loaded.
        if (Permissions != null)
            return;
                
        // Get permissions plugin.
        Plugin testPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
        
        // If we were able to get the Permissions plugin.
        if (testPermissions != null) {
            Permissions = ((Permissions) testPermissions).getHandler();
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
