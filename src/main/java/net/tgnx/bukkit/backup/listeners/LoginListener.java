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

package net.tgnx.bukkit.backup.listeners;

import net.tgnx.bukkit.backup.config.Settings;
import net.tgnx.bukkit.backup.config.Strings;
import net.tgnx.bukkit.backup.threading.LastBackupTask;
import net.tgnx.bukkit.backup.utils.LogUtils;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Kilian Gaertner
 */
public class LoginListener extends PlayerListener {

    private int taskID = -2;
    private Settings settings;
    private Plugin plugin;
    private Strings strings;

    public LoginListener (Plugin plugin, Settings settings) {
        this.settings = settings;
        this.plugin = plugin;
        this.strings = new Strings(plugin);
    }

    @Override
    public void onPlayerLogin (PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();


        if (taskID != -2 && server.getOnlinePlayers().length == 0) {
            server.getScheduler().cancelTask(taskID);
            LogUtils.sendLog(strings.getString("stoppedlast"));
            taskID = -2;
        }
    }

    @Override
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (server.getOnlinePlayers().length <= 1) {
            int interval = settings.getIntProperty("backupinterval");
            if (interval != -1) {
                interval *= 1200;
                taskID = server.getScheduler().scheduleSyncDelayedTask(plugin, new LastBackupTask(server, settings), interval);
                LogUtils.sendLog(strings.getStringWOPT("lastbackup", Integer.toString(interval / 1200)));
            }
            
        }
    }
}