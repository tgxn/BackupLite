/*
 *  Backup - CraftBukkit server backup plugin. (continued 1.8+)
 *  @author Lycano <https://github.com/lycano/>
 * 
 *  Backup - CraftBukkit server backup plugin. (continued 1.7+)
 *  @author Domenic Horner <https://github.com/gamerx/>
 *
 *  Backup - CraftBukkit server backup plugin. (original author)
 *  @author Kilian Gaertner <https://github.com/Meldanor/>
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

package net.tgxn.bukkit.backup.listeners;

import net.tgxn.bukkit.backup.BackupMain;
import net.tgxn.bukkit.backup.config.Properties;
import net.tgxn.bukkit.backup.config.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.tgxn.bukkit.backup.threading.PrepareBackupTask;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;

public class CommandListener extends PlayerListener implements CommandExecutor {

    private PrepareBackupTask backupTask;
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
    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        // If an In-Game Command, check permissions.
        if((sender instanceof Player)) {
            player = (Player) sender;
            if (BackupMain.Permissions != null) {
                 if(!BackupMain.Permissions.has(player, "backup.backup"))
                    player.sendMessage(strings.getString("norights"));
                 return true;
            } else if (properties.getBooleanProperty("onlyops") && !player.isOp()) {
                 player.sendMessage(strings.getString("norights"));
                 return true;
            }
        }
        
        // Actual commands.
        if (label.equalsIgnoreCase("backup")) {
            backupTask.setAsManualBackup();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
        }
        
       return true;
    }
    
}
