/*
 *  Backup - CraftBukkit Server Backup Plugin.
 *   
 *  Copyright - Domenic Horner, lycano, Kilian Gaertner.
 *  URL: https://github.com/gamerx/Backup
 * 
 *  Please read README and LICENSE for more details.
 */

package net.tgxn.bukkit.backup;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.listeners.CommandListener;
import net.tgxn.bukkit.backup.listeners.LoginListener;
import net.tgxn.bukkit.backup.threading.PrepareBackupTask;
import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class BackupMain extends JavaPlugin {
     
    public static PermissionHandler Permissions;
    protected static Strings strings;
    protected static Settings settings;
    private PrepareBackupTask preparedBackupTask;
    public int mainBackupTaskID;

    @Override
    public void onLoad() {
         
        // Init LogUtils, for logging purposes.
        LogUtils.initLogUtils(this);
        
        // Check the plugin's data folder exists.
        if (!this.getDataFolder().exists()) {
            
            // Try to create the folder.
            try {
                this.getDataFolder().mkdirs();
            } catch(SecurityException se) {
                
                // Advise this failed.
                LogUtils.sendLog(Level.SEVERE, "Failed to create plugin's data folder: Security Exception." );
                //se.printStackTrace(System.out);
            }
        }
        
        // Load Properties, create if needed.
        settings = new Settings(this);
        
        // Load Strings, create if needed.
        strings = new Strings(this);

        // Check the specified backup folder exists.
        File backupsFolder = new File(settings.getStringProperty("backuppath"));
        if (!backupsFolder.exists()) {
            
            // Try to create the folder.
            try {
                 if(backupsFolder.mkdirs())
                    LogUtils.sendLog(strings.getString("createbudir"));
            } catch(SecurityException se) {
                
                // Advise this failed.
                LogUtils.sendLog(Level.SEVERE, "Failed to create backup folder: Security Exception." );
                //se.printStackTrace(System.out);
            }
        }
    }
    
    @Override
    public void onEnable () {
        
        // Check and load permissions system.
        setupPermissions();

        // Get server object.
        Server server = getServer();

        // Get PluginManager object.
        PluginManager pm = server.getPluginManager();

        // Setup the scheduled BackupTask.
        preparedBackupTask = new PrepareBackupTask(server, settings, strings);

        // Setup the CommandListener, for commands.
        getCommand("backup").setExecutor(new CommandListener(preparedBackupTask, settings, strings, this));

        // Setup LoginListener if we require it.
        if (settings.getBooleanProperty("backuponlywithplayer")) {
            LoginListener loginListener = new LoginListener(this, settings, strings);
            pm.registerEvent(Type.PLAYER_LOGIN, loginListener, Priority.Normal, this);
            pm.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        }

        // Setup the scheduled backuptask, or turn it off if not needed.
        int interval = settings.getIntProperty("backupinterval");
        if (interval != -1) {
            interval *= 1200;
            mainBackupTaskID = server.getScheduler().scheduleSyncRepeatingTask(this, preparedBackupTask, interval, interval);
        } else
            LogUtils.sendLog(strings.getString("disbaledauto"));

        // Inform Startup Complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }
    
    @Override
    public void onDisable () {
        
        // Cancell any scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Inform shutdown successfull.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed un-loading!", false);
    }
     
    
    /**
     * Check if the Permissions System is available.
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
    
}
