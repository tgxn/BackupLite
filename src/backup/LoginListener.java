/*
 *  Copyright (C) 2011 Kilian Gaertner
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

import threading.LastBackupTask;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Kilian Gaertner
 */
public class LoginListener extends PlayerListener {

    private int taskID = -2;
    private Properties properties;
    private Plugin plugin;
    private Strings strings;

    public LoginListener (Plugin plugin, Properties properties) {
        this.properties = properties;
        this.plugin = plugin;
        this.strings = new Strings(plugin);
    }

    @Override
    public void onPlayerLogin (PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        
 
        if (taskID != -2 && server.getOnlinePlayers().length == 0) {
            server.getScheduler().cancelTask(taskID);
            System.out.println(strings.getString("stoppedlast"));
            taskID = -2;
        }
    }

    @Override
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (server.getOnlinePlayers().length <= 1) {
            int interval = properties.getIntProp("backupinterval");
            if (interval != -1) {
                interval *= 1200;
                taskID = server.getScheduler().scheduleSyncDelayedTask(plugin, new LastBackupTask(server, properties), interval);
                System.out.println(strings.getStringWOPT("lastbackup", Integer.toString(interval / 1200)));
            }
            
        }
    }
}
