package com.bukkitbackup.lite.utils;

import java.io.File;

/**
 * I don't like repeating myself. - Domenic H (2011)
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
                LogUtils.exceptionLog(se);
            }
        }
        return false;
    }
}
