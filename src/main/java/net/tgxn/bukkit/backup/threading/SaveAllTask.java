package net.tgxn.bukkit.backup.threading;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

public class SaveAllTask implements Runnable {

    private final Server server;

    public SaveAllTask (Server server) {
        this.server = server;
    }

    @Override
    public void run () {
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();
        server.dispatchCommand(consoleCommandSender, "save-all");
    }
}
