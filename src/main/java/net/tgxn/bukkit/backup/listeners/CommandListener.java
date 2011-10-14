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

package net.tgxn.bukkit.backup.listeners;

import java.io.File;
import net.tgxn.bukkit.backup.BackupMain;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.threading.PrepareBackupTask;
import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;

public class CommandListener extends PlayerListener implements CommandExecutor {

    private PrepareBackupTask backupTask = null;
    private Settings settings;
    private final Plugin plugin;
    private Strings strings;
    
    /**
     * The main constructor to initalize listening for commands.
     * 
     * @param backupTask The backuptask.
     * @param settings Load settings for the plugin.
     * @param strings The strings configuration for th plugin.
     * @param plugin The plugin object itself
     */
    public CommandListener(PrepareBackupTask backupTask, Settings settings, Strings strings, Plugin plugin) {
        this.backupTask = backupTask;
        this.settings = settings;
        this.plugin = plugin;
        this.strings = strings;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Initalize variables.
        Player player = null;

        // Process in-game commands.
        if((sender instanceof Player)) {

            // Get player object
            player = (Player) sender;
        }
        
        // Do the actual processing.
        return processCommand(label, args, player);
    }
    
    
    /**
     * Method to process every command.
     * 
     * @param command The command (Usually "backup")
     * @param args Arguments passed along with the command.
     * @param player The player that requested the command.
     * @return True is success, False if fail.
     */
    public boolean processCommand(String command, String[] args, Player player) {
            
        if(player == null) {
            // Only console command will be "backup"
            if (command.equalsIgnoreCase("backup"))
                doManualBackup();
        } else {
        // For all playercommands.
            
            
            
            // For everything under the backup command.
            if (command.equalsIgnoreCase("backup")) {
            
            
                // Contains auguments.
                if(args.length > 0) {
                    if(args[0].equals("help"))
                        if(checkPerms(player, "backup.help"))
                            sendHelp(player);
                    
                    if(args[0].equals("reload"))
                        if(checkPerms(player, "backup.reload"))
                            // @TODO Implement reload method.
                    
                    if(args[0].equals("list"))
                        if(checkPerms(player, "backup.list"))
                            listBackups(player);
                    
                    if(args[0].equals("config"))
                        if(checkPerms(player, "backup.config"))
                            showConfig(player);
                        
                    if(args[0].equals("log"))
                        if(checkPerms(player, "backup.log"))
                            showLog(player);
                } else {
                    if(checkPerms(player, "backup.backup"))
                        doManualBackup();    
                }
            }

        }
        return true;
    }
    
    /**
     * Start a manual backup.
     */
    private void doManualBackup() {
        backupTask.setAsManualBackup();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, backupTask);
    }
    
    /**
     * Send the plugin help to the player.
     * 
     * @param player The player who requested the help.
     */
    private void sendHelp(Player player) {
        player.sendMessage("Backup v"+plugin.getDescription().getVersion()+" Help Menu");
        player.sendMessage("Commands:");
        player.sendMessage("/backup");
        player.sendMessage("- Performs a backup.");
        player.sendMessage(".");
        player.sendMessage(".");
        player.sendMessage("Coming Soon :)");
        player.sendMessage(".");
        player.sendMessage(".");
        player.sendMessage(".");
    }
    
    /**
     * Checks if the player has permissions.
     * Also sends a message if the player does not have permissions.
     * 
     * @param player The player's object.
     * @param permission The name of the permission
     * @return True if they have permission, false if no permission
     */
    private boolean checkPerms(Player player, String permission) {

        // We hooked a perms system.
        if (BackupMain.Permissions != null) {
            if (!BackupMain.Permissions.has(player, permission)) {
                player.sendMessage(strings.getString("norights"));
                return false;
            } else {
                return true;
            }

        } else {
            
            // Check what to do in case of no permissions.
            if (settings.getBooleanProperty("onlyops") && !player.isOp()) {
                player.sendMessage(strings.getString("norights"));
                return false;
            } else {
                return true;
            }
        }
    }
    
    
    /**
     * For listing all the backups for a user.
     * Lists to a maximum of 8 so that it doesn't flow off screen.
     * 
     * @param player The player that requested the list.
     */
    private void listBackups(Player player) {
        
        // Get the backups path.
        String backupDir = settings.getStringProperty("backuppath");
        
        // Make a list.
        String[] filesList = new File(backupDir).list();
        
        // Inform what is happenning.
        player.sendMessage("Listing backup directory: \"" + backupDir + "\".");
        
        // Check if the directory exists.
        if (filesList == null) {
            
            // Error message.
            player.sendMessage("Error listing directory!");
        } else {
            
            // How many files in array.
            int amountoffiles = filesList.length;
            
            // Limit listings, so it doesnt flow off screen.
            if(amountoffiles > 8) {
                amountoffiles = 8;
            }
            
            // Send informal message.
            player.sendMessage(""+amountoffiles+" backups found, listing...");
            
            // Loop through files, and list them.
            for (int i=0; i<amountoffiles; i++) {
                
                // Get filename of file.
                String filename = filesList[i];
                
                // Send messages for each file.
                int number = i + 1;
                player.sendMessage(number+"). "+filename);
            }
        }
    }
    
    /**
     * To show the plugins configuration.
     * 
     * @param player The player that requested the configuration.
     */
    private void showConfig(Player player) {
        
        player.sendMessage("Backup Configuration");
        
        int interval = settings.getIntProperty("backupinterval");
        if(interval != -1) {
            player.sendMessage("Scheduled Backups: Enabled, "+interval+" mins between backups.");
        } else{
            player.sendMessage("Scheduled backups: Disabled, Manual backups only.");
        }  
        
        boolean hasToZIP = settings.getBooleanProperty("zipbackup");
        if(hasToZIP)
            player.sendMessage("Backup compression is Enabled.");
        else
            player.sendMessage("Backup compression is Disabled.");
    }

    private void showLog(Player player) {
        player.sendMessage("Coming Soon :)");
    }
}
