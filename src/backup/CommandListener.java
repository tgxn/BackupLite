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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import threading.PrepareBackupTask;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;


/**
 * For manual backups
 * @author Kilian Gaertner
 */
public class CommandListener extends PlayerListener implements CommandExecutor {

    private PrepareBackupTask backupTask = null;
    private Properties properties;
    private final Plugin plugin;
    private Strings strings;
    private Player player;
    

    public CommandListener (PrepareBackupTask backupTask, Properties properties, Plugin plugin) {
        this.backupTask = backupTask;
        this.properties = properties;
        this.plugin = plugin;
        this.strings = new Strings(plugin);
    }

    
    @Override
    public boolean onCommand (CommandSender cs, Command command, String label, String[] args) {
        if((cs instanceof Player)) { // In-Game Command
            
            // Get player object
            player = (Player)cs;
            
            // Verify Permissions
            if (Main.Permissions != null) { //permissions loaded
                 if(!Main.Permissions.has(player, "backup.backup"))
                    player.sendMessage(strings.getString("norights"));
                 return true;
            } else if (properties.getBooleanProp("onlyops") && !player.isOp()) {
                 player.sendMessage(strings.getString("norights"));
                 return true;
            }
        }
        
        if (label.equalsIgnoreCase("backup")) {
            backupTask.setAsManualBackup();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
        }
        
       return true;
    }
    
}
