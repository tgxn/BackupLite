package com.bukkitbackup.lite.events;

import com.bukkitbackup.lite.config.Settings;
import com.bukkitbackup.lite.threading.PrepareBackup;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class CommandHandler implements Listener, CommandExecutor {

    private PrepareBackup prepareBackup = null;
    private Plugin plugin;
    private Server server;
    private Settings settings;
    
    /**
     * This class is used to listen for console and player commands. It also
     * contains methods to handle them, and provide output.
     *
     * @param prepareBackup Instance of the prepareBackup.
     * @param plugin Instance of the JavaPlugin.
     * @param settings Instance of the settings loader.
     * @param strings Instance of the strings loader.
     */
    public CommandHandler(PrepareBackup prepareBackup, Plugin plugin, Settings settings) {
        this.prepareBackup = prepareBackup;
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.settings = settings;
    }

    /**
     * Called whenever a command is sent.
     *
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Perform the command procesing.
        return processCommand(sender, command, label, args);
    }

    /**
     * Method to process every command.
     *
     * @param command The command (Usually "backup")
     * @param args Arguments passed along with the command.
     * @param player The player that requested the command.
     * @return True is success, False if fail.
     */
    public boolean processCommand(CommandSender sender, Command command, String label, String[] args) {

        // For commands we actually handle.
        if (label.equalsIgnoreCase("backup") || label.equalsIgnoreCase("bu")) {

            // Check if arguments were specified.
            if (args.length == 0) {

                // Main command, perform manual backup.
                if (checkPerms(sender, "backup.backup")) {
                    doManualBackup();
                }

            } else if (args.length == 1) {

                // Reload command - Reloads plugin.
                if (args[0].equals("reload")) {
                    if (checkPerms(sender, "backup.reload")) {
                        reloadPlugin(sender);
                    }
                }

            } else {
                sender.sendMessage("Error: Command unknown.");
            }
        }

        // Return true for manager.
        return true;
    }

    /**
     * Performs a manual backup.
     */
    private void doManualBackup() {

        // Sets this as a manual backup in the preperation stage.
        prepareBackup.setAsManualBackup();

        // Schedule an async task to run for the backup.
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, prepareBackup);
    }

    /**
     * Reload, and report success.
     *
     * @param sender The CommandSender.
     */
    public void reloadPlugin(CommandSender sender) {
        plugin.onDisable();
        plugin.onLoad();
        plugin.onEnable();
        sender.sendMessage("Reloaded ok.");
    }

    /**
     * Checks if the player has permissions. Also sends a message if the player
     * does not have permissions.
     *
     * @param player The player's object.
     * @param permissionNode The name of the permission
     * @return True if they have permission, false if no permission
     */
    private boolean checkPerms(CommandSender sender, String permissionNode) {

        // Check if sender is player or not.
        if ((sender instanceof Player)) {
            Player player = (Player) sender;

            // Check the player has permission set.
            if (player.isPermissionSet(permissionNode)) {

                // Check player for permissions node.
                if (!player.hasPermission(permissionNode)) {
                    player.sendMessage("You do not have enough rights to perform this command.");
                    return false;
                } else {
                    return true;
                }

            } else {

                // Check what to do in case of no permissions.
                if (settings.getBooleanProperty("onlyops") && !player.isOp()) {
                    player.sendMessage("You do not have enough rights to perform this command.");
                    return false;
                } else {
                    return true;
                }
            }

        } else {

            // Console session.
            return true;
        }
    }
}
