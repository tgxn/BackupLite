package net.tgxn.bukkit.backup.utils;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

public class SyncSaveAllUtil implements Runnable {

    private final Server server;
    private int mode;

    public SyncSaveAllUtil(Server server, int mode) {
        this.server = server;
        this.mode = mode;
    }

    @Override
    public void run() {
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();
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