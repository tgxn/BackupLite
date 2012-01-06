package net.tgxn.bukkit.backup.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * I don't like repeating myself.
 * 
 * @author Domenic Horner
 */
public class SharedUtils {
    
    /**
     * Checks if a folder exists and creates it if it does not.
     * 
     * @param toCheck File to check.
     * @return True if created, false if exists.
     */
    public static boolean checkFolderAndCreate(File toCheck) {
        if (!toCheck.exists()) {
            try {
                if (toCheck.mkdirs()) {
                    return true;
                }
            } catch (SecurityException se) {
                LogUtils.exceptionLog(se.getStackTrace());
            }
        }
        return false;
    }
    
}
