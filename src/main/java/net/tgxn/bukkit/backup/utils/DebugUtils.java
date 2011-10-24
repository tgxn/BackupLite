package net.tgxn.bukkit.backup.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public class DebugUtils {
    
    private static String debuglog = "debug.log";
    private static Plugin plugin;
    
    public static void initDebugUtils(Plugin plugin) {            
            DebugUtils.plugin = plugin;
    }
    
    public static void debugLog(StackTraceElement ste[], String message) {
        LogUtils.sendLog(Level.SEVERE, message, true);
        debugLog(ste);
    }
    
    public static void debugLog(StackTraceElement ste[]) {

        
        String toSystemOut = "";
        String[] error = null;
        for (int i = 0; i < ste.length; i++) {
            
            toSystemOut =  ste[i].getFileName() + ":" + ste[i].getLineNumber();
            
            error[0] = ste[i].getFileName() + ":" + ste[i].getLineNumber() + " ==>" + ste[i].getMethodName() + "()";
            error[1] = ste[i].toString();
        }

        // File 
        try {
            FileWriter debugFW = new FileWriter(plugin.getDataFolder() + debuglog, true);
            
            debugFW.write(error[0]);
            debugFW.write(error[1]);
            
            String tags = "[" + plugin.getDescription().getName()  + "] ";
            
            System.out.println(tags + "Error: "+toSystemOut);
            
            plugin.getServer().broadcastMessage(tags + " Plugin encountered an exception, check debug log for details.");
            
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
        }
    }
}
