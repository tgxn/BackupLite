package com.bukkitbackup.lite.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class LogUtils {

    private static Plugin plugin;
    private static Logger logger;

    public static void initLogUtils(Plugin plugin) {
        if (LogUtils.logger == null) {
            if (plugin != null) {
                LogUtils.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + plugin.getServer().getName());
            }
            LogUtils.plugin = plugin;
        }
    }

    public static void exceptionLog(Throwable ste, String message) {
        sendLog(message);
        exceptionLog(ste);
    }

    public static void exceptionLog(Throwable ste) {
        sendLog("Please provide following error with support request:");
        ste.printStackTrace(System.out);
    }

    public static void sendLog(String message) {
        sendLog(message, true);
    }
    public static void sendLog(String message, boolean tags) {
        if(tags)
            logger.log(Level.INFO, "[" + plugin.getDescription().getName() + "] " + message);
        else
            logger.log(Level.INFO, message);
    }
}
