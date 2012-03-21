package net.tgxn.bukkit.backup.events;

import java.io.File;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.config.UpdateChecker;
import net.tgxn.bukkit.backup.threading.PrepareBackup;
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
    private Strings strings;
    private UpdateChecker updateChecker;

    /**
     * This class is used to listen for console and player commands. It also
     * contains methods to handle them, and provide output.
     *
     * @param prepareBackup Instance of the prepareBackup.
     * @param plugin Instance of the JavaPlugin.
     * @param settings Instance of the settings loader.
     * @param strings Instance of the strings loader.
     */
    public CommandHandler(PrepareBackup prepareBackup, Plugin plugin, Settings settings, Strings strings, UpdateChecker updateChecker) {
        this.prepareBackup = prepareBackup;
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.settings = settings;
        this.strings = strings;
        this.updateChecker = updateChecker;
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
                } // Version command - Version information.
                else if (args[0].equals("ver")) {
                    if (checkPerms(sender, "backup.ver")) {
                        showVersion(sender);
                    }
                } // Help command - Show help & support info.
                else if (args[0].equals("help")) {
                    if (checkPerms(sender, "backup.help")) {
                        showHelp(sender);
                    }
                } // List backups - Default 8.
                else if (args[0].equals("list")) {
                    if (checkPerms(sender, "backup.list")) {
                        listBackups(sender, 8);
                    }
                } // Unknown command.
                else {
                    sender.sendMessage(strings.getString("unknowncommand"));
                }

            } else if (args.length == 2) {

                // List backups - Set amount.
                if (args[0].equals("list")) {
                    if (checkPerms(sender, "backup.list")) {
                        listBackups(sender, Integer.parseInt(args[1]));
                    }
                } // Unknown command.
                else {
                    sender.sendMessage(strings.getString("unknowncommand"));
                }

                // Unknown command.
            } else {
                sender.sendMessage(strings.getString("unknowncommand"));
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
        sender.sendMessage(strings.getString("reloadedok", plugin.getDescription().getVersion()));
    }

    /**
     * Show version method.
     *
     * @param sender The CommandSender.
     */
    private void showVersion(final CommandSender sender) {

        // Notify the caller.
        sender.sendMessage(strings.getString("gettingversions"));

        // Start a new asynchronous task to get version and print them.
        server.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {

                // Attempt to retrieve latest version.
                String latestVersion = updateChecker.getVersion();

                // Set up current version.
                String currentVersion = plugin.getDescription().getVersion();

                String upToDate = strings.getString("outofdate");
                if (latestVersion.equals(currentVersion)) {
                    upToDate = strings.getString("atlatestversion");
                }

                // Check for null.
                if (latestVersion == null) {
                    latestVersion = strings.getString("unknownfailedversion");
                    upToDate = strings.getString("unknownfailedversion");
                }

                // Notify the user.
                sender.sendMessage("Version Information for " + plugin.getDescription().getName());
                sender.sendMessage(" ");
                sender.sendMessage("Version Status: " + upToDate);
                sender.sendMessage(" ");
                sender.sendMessage("Loaded Version: " + plugin.getDescription().getVersion() + ".");
                sender.sendMessage("Latest Version: " + latestVersion + ".");
                sender.sendMessage(" ");
            }
        });
    }

    /**
     * Command to list help information to th sender.
     *
     * @param sender The CommandSender.
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getDescription().getName() + " Help Menu");
        sender.sendMessage(" ");
        sender.sendMessage("Website: bukkitbackup.com");
        sender.sendMessage("Email: bugs@bukkitbackup.com");
        sender.sendMessage(" ");
        sender.sendMessage("Dev Info");
        sender.sendMessage("CI: ci.tgxn.net");
        sender.sendMessage("BukktiDev: dev.bukkit.org/server-mods/backup");
        sender.sendMessage(" ");
    }

    /**
     * List the backups in the backup folder. We can use the parameter to limit
     * the number of results.
     *
     * @param sender The CommandSender.
     * @param amount The amount of results we want.
     */
    private void listBackups(CommandSender sender, int amount) {

        // Get the backups path.
        String backupDir = settings.getStringProperty("backuppath");

        // Make a list.
        String[] filesList = new File(backupDir).list();

        // Inform what is happenning.
        sender.sendMessage("Listing backup directory: \"" + backupDir + "\".");

        // Check if the directory exists.
        if (filesList == null) {

            // Error message.
            sender.sendMessage(strings.getString("errorfolderempty"));
        } else {

            // How many files in array.
            int amountoffiles = filesList.length;

            // Limit listings, so it doesnt flow off screen.
            if (amountoffiles > amount) {
                amountoffiles = amount;
            }

            // Send informal message.
            sender.sendMessage("" + amountoffiles + " backups found, listing...");

            // Loop through files, and list them.
            for (int i = 0; i < amountoffiles; i++) {

                // Get filename of file.
                String filename = filesList[i];

                // Send messages for each file.
                int number = i + 1;
                sender.sendMessage(number + "). " + filename);
            }
        }
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

        } else {

            // Console session.
            return true;
        }
    }
}
