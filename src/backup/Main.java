/*
 *  Copyright (C) 2011 Kilian Gaertner
 *  Modified      2011 Domenic Horner
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

package backup;

import threading.PrepareBackupTask;
import java.io.File;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class Main extends JavaPlugin {

    public static PermissionHandler Permissions;
    private PrepareBackupTask run;
    protected static Strings strings;
    protected static Properties properties;
    
    @Override
    public void onEnable () {
        
        // Check plugin Data Folder, create if not exist.
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();
        
        // Load Properties.
        properties = new Properties(this);
        
        // Load Strings.
        strings = new Strings(this);
        
        // Check and load permissions system.
        setupPermissions();
        
        // Check backup folder, create if needed
        // Query: Can this be relative AND absolute? (Read: Doe it WORK?)
        File plugindatafolder = new File(properties.getStringProp("backuppath"));
        if (!plugindatafolder.exists()) {
            plugindatafolder.mkdirs();
            System.out.println(strings.getString("createbudir"));
        }
        
        // Get server object.
        Server server = getServer();
        
        // Get PluginManager object.
        PluginManager pm = server.getPluginManager();

        // Setup the sceduled BackupTask.
        run = new PrepareBackupTask(server, properties);

        // Setup the CommandListener, for commands.
        getCommand("backup").setExecutor(new CommandListener(run, properties, this));
        
        // Setup LoginListener if we require it.
        if (properties.getBooleanProp("backuponlywithplayer")) {
            LoginListener loginlistener = new LoginListener(this, properties);
            pm.registerEvent(Type.PLAYER_LOGIN, loginlistener, Priority.Normal, this);
            pm.registerEvent(Type.PLAYER_QUIT, loginlistener, Priority.Normal, this);
        }
        
        // Setup the sceduled backuptask, or turn it off if not needed.
        int interval = properties.getIntProp("backupinterval");
        if (interval != -1) {
            interval *= 1200;
            server.getScheduler().scheduleSyncRepeatingTask(this, run, interval, interval);
        } else
            System.out.println(strings.getString("disbaledauto"));
        
        // Inform Startup Complete.
        System.out.println(this.getDescription().getFullName() + " was sucessfully loaded!");
    }
    
    @Override
    public void onDisable () {
        
        // Cancell any sceduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Inform shutdown successfull
        System.out.println(this.getDescription().getFullName() + " was sucessfully unloaded!");
    }
     
    
    // Check if the Permissions System is available.
    private void setupPermissions () {
        Plugin loadPerms = this.getServer().getPluginManager().getPlugin("Permissions");
        if (Permissions == null)
            if (loadPerms != null) {
                Permissions = ((Permissions) loadPerms).getHandler();
                System.out.println(strings.getString("hookedperms"));
            } else
                System.out.println(strings.getString("defaultperms"));
    }
}
