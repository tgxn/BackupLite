package net.tgxn.bukkit.backup.threading;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

public class SyncSaveAll implements Runnable {

    private final Server server;
    private int mode;

    /**
     * This class is used for synchronizing the save-all task.
     * It is started as a new thread.
     *
     * @param server The server object for this plugin.
     * @param mode The type of save-all we are performing.
     */
    public SyncSaveAll(Server server, int mode) {
        this.server = server;
        this.mode = mode;
    }

    /**
     * The run method gets the sender, and dispatches commands.
     */
    @Override
    public void run() {

        // Get the ConsoleCommandSender instance for use.
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();

        // Switch for the modes, and perform the command
        // @TODO Find a better method of passing the option.
        switch (mode) {
            case 0:
                server.dispatchCommand(consoleCommandSender, "save-all");
                break;
            case 1:
                server.dispatchCommand(consoleCommandSender, "save-all");
                server.dispatchCommand(consoleCommandSender, "save-off");
                break;
            case 2:
                server.dispatchCommand(consoleCommandSender, "save-on");
                break;
            default:
                server.dispatchCommand(consoleCommandSender, "save-all");
                break;
        }
    }
}
