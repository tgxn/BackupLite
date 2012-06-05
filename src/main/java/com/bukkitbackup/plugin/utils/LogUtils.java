package com.bukkitbackup.plugin.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class LogUtils {

    // Private veriables for this class.
    private static Level logLevel = Level.INFO;
    private static Logger logger;
    private static Plugin plugin;
    private static boolean logToConsole;

    /**
     * Main Constructor for LogUtils. Creates logger, sets default log level and
     * variables.
     *
     * @param plugin The plugin's object.
     */
    public static void initLogUtils(Plugin plugin) {
        if (LogUtils.logger == null) {
            if (plugin != null) {
                LogUtils.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + plugin.getServer().getName());
            }

            LogUtils.logLevel = Level.INFO;
            LogUtils.logger.setLevel(Level.INFO);
            LogUtils.plugin = plugin;
            
        }
    }

    public static void finishInitLogUtils(boolean logToConsole) {
        LogUtils.logToConsole = logToConsole;
    }

    /**
     * Exception handling, Called instead of sendLog so it can be debugged.
     *
     * @param ste The stack trace.
     * @param message Message accompanying it.
     */
    public static void exceptionLog(Throwable ste, String message) {
        sendLog(message, Level.SEVERE, true);
        exceptionLog(ste);
    }

    /**
     * Parse the STE and print the error.
     *
     * @param ste The stack trace element.
     */
    public static void exceptionLog(Throwable ste) {
        sendLog("Please provide following error with support request:", Level.SEVERE, true);
        ste.printStackTrace(System.out);
    }

    /**
     * Sends log message.
     *
     * @param message
     */
    public static void sendLog(String message) {
        sendLog(message, Level.INFO, true);
    }

    /**
     * Sends log message.
     *
     * @param message
     * @param tags
     */
    public static void sendLog(String message, boolean tags) {
        sendLog(message, Level.INFO, tags);
    }

    /**
     * Sends log message.
     *
     * @param message
     * @param tags
     */
    public static void sendLog(Level level, String message) {
        sendLog(message, level, true);
    }

    /**
     * Sends log message.
     *
     * @param logLevel Logger level for this item.
     * @param message The text of the log.
     * @param addTags Should we add tags to the string.
     */
    public static void sendLog(String message, Level logLevel, boolean addTags) {
        final String nameTag = ("[" + plugin.getDescription().getName() + "] ");
        if (addTags) {
            message = nameTag + message;
        }
        if (logToConsole) {
            logger.log(logLevel, message);
        }
    }

    /**
     * Sets the default LoglLevel.
     *
     * @param logLevel The level to set the logger to.
     */
    public static void setLogLevel(Level logLevel) {
        LogUtils.logLevel = logLevel;
        LogUtils.logger.setLevel(logLevel);
    }

    /**
     * Gets the current LogLevel.
     *
     * @return The current log level.
     */
    public static Level getLogLevel() {
        return logLevel;
    }
}
