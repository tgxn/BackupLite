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

package de.luricos.bukkit.backup;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import de.luricos.bukkit.backup.config.Properties;
import de.luricos.bukkit.backup.config.Strings;
import de.luricos.bukkit.backup.listeners.CommandListener;
import de.luricos.bukkit.backup.listeners.LoginListener;
import de.luricos.bukkit.backup.threading.PrepareBackupTask;
import de.luricos.bukkit.backup.utils.BackupLogger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class BackupPlugin extends JavaPlugin {
    public static PermissionHandler Permissions;

    protected static Strings strings;
    protected static Properties properties;

    private PrepareBackupTask preparedBackupTask;

    public void onLoad() {
        BackupLogger.initLogger("Backup", this.getDescription().getVersion(), Level.INFO);
        
        BackupLogger.prettyLog(true, "Startup in progress ...");
        
        // init config
        // Check plugin Data Folder, create if not exist.
        if (!this.getDataFolder().exists()) {
            //@TODO create try catch exception class on error
            this.getDataFolder().mkdirs();
        }

        // Check backup folder, create if needed
        File pluginDataFolder = new File(properties.getStringProperty("backuppath"));
        if (!pluginDataFolder.exists()) {
            //@TODO create try catch exception class on error
            pluginDataFolder.mkdirs();
            BackupLogger.prettyLog(strings.getString("createbudir"));
        }

        BackupLogger.prettyLog(true, "Loading completed");
    }    
    
    @Override
    public void onEnable () {
        // Load Properties.
        properties = new Properties(this);

        // Load Strings.
        strings = new Strings(this);

        // Check and load permissions system.
        setupPermissions();

        // Get server object.
        Server server = getServer();

        // Get PluginManager object.
        PluginManager pm = server.getPluginManager();

        // Setup the scheduled BackupTask.
        preparedBackupTask = new PrepareBackupTask(server, properties);

        // Setup the CommandListener, for commands.
        getCommand("backup").setExecutor(new CommandListener(preparedBackupTask, properties, this));

        // Setup LoginListener if we require it.
        if (properties.getBooleanProperty("backuponlywithplayer")) {
            LoginListener loginListener = new LoginListener(this, properties);
            pm.registerEvent(Type.PLAYER_LOGIN, loginListener, Priority.Normal, this);
            pm.registerEvent(Type.PLAYER_QUIT, loginListener, Priority.Normal, this);
        }

        // Setup the scheduled backuptask, or turn it off if not needed.
        int interval = properties.getIntProperty("backupinterval");
        if (interval != -1) {
            interval *= 1200;
            server.getScheduler().scheduleSyncRepeatingTask(this, preparedBackupTask, interval, interval);
        } else {
            BackupLogger.prettyLog(strings.getString("disbaledauto"));
        }

        // Inform Startup Complete.
        BackupLogger.prettyLog(true, " sucessfully enabled!");
    }
    
    @Override
    public void onDisable () {
        // Cancell any scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Inform shutdown successfull
        BackupLogger.prettyLog(true, " sucessfully unloaded!");
    }
     
    
    // Check if the Permissions System is available.
    private void setupPermissions () {
        Plugin testPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
        if (Permissions != null)
            return;

        if (testPermissions != null) {
            Permissions = ((Permissions) testPermissions).getHandler();
            BackupLogger.prettyLog(strings.getString("hookedperms"));
        } else {
            BackupLogger.prettyLog(strings.getString("defaultperms"));
        }
    }
}