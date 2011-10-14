/*
 *  Backup - CraftBukkit server Backup plugin (continued)
 *  Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
 *  Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
 *
 *  Backup - CraftBukkit server Backup plugin (original author)
 *  Copyright (C) 2011 Kilian Gaertner <https://github.com/Meldanor/Backup>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

public class BackupMain extends JavaPlugin {
     
    public static PermissionHandler Permissions;
    protected static Strings strings;
    protected static Settings settings;
    private PrepareBackupTask preparedBackupTask;
    public int mainBackupTaskID;
    
    @Override
    public void onLoad() {
         
        // Init LogUtils.
        LogUtils.initLogUtils(this);
        
        // Check plugin Data Folder, create if not exist.
        if (!this.getDataFolder().exists()) {
            // @TODO create try catch exception class on error
            this.getDataFolder().mkdirs();
        }
        
        // Load Properties, create if needed.
        settings = new Settings(this);
        
        // Load Strings, create if needed.
        strings = new Strings(this);

        // Check backup folder, create if needed.
        File backupsFolder = new File(settings.getStringProperty("backuppath"));
        if (!backupsFolder.exists()) {
            //@TODO create try catch exception class on error
            backupsFolder.mkdirs();
            LogUtils.sendLog(strings.getString("createbudir"));
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
