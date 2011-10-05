/*
 *  Backup - CraftBukkit server Backup plugin (continued)
 *  Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
 *  Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
 *
 *  Backup - CraftBukkit server Backup plugin (original author)
 *  Copyright (C) 2011 Kilian Gaertner <https://github.com/Meldanor/Backup>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tgxn.bukkit.backup.threading;

import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.utils.FileUtils;
import net.tgxn.bukkit.backup.utils.LogUtils;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;

import static net.tgxn.bukkit.backup.utils.FileUtils.FILE_SEPARATOR;

/**
 * The Task copies and backups the worlds and delete older backups. This task
 * is only runes once in backup and doing all the thread safe options.
 * The PrepareBackupTask and BackupTask are two threads to find a compromise between
 * security and performance.
 *
 * @author Kilian Gaertner
 */
public class BackupTask implements Runnable {

    private final Settings settings;
    private Strings strings;
    private Plugin plugin;
    private final LinkedList<String> worldsToBackup;
    private final Server server;

    public BackupTask(Settings settings, Strings strings, LinkedList<String> worldsToBackup, Server server) {
        this.settings = settings;
        this.worldsToBackup = worldsToBackup;
        this.server = server;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = strings;
    }

    @Override
    public void run() {
        try {
            backup();
        } catch (Exception ex) {
            /** @TODO create exception classes **/
            ex.printStackTrace(System.out);
        }
    }

    /**
     * Run the backup.
     * 
     * @throws Exception 
     */
    public void backup() throws Exception {
        
        // Load preferences.
        String backupDirName = settings.getStringProperty("backuppath").concat(FILE_SEPARATOR);
        boolean ShouldZIP = settings.getBooleanProperty("zipbackup");
        boolean BackupWorlds = settings.getBooleanProperty("backupworlds");
        boolean BackupPlugins = settings.getBooleanProperty("backupplugins");
        
        // Store backups in one container
        if (settings.getBooleanProperty("singlebackup")) {
            
            // Folder to store backup in. IE: "backups/30092011-142238"
            backupDirName = backupDirName.concat(getFolderName());
            
            // If there are worlds to backup, and we are performing world backups.
            if ((worldsToBackup != null) && (BackupWorlds)) {
                
                // While we have a worlds to backup.
                while (!worldsToBackup.isEmpty()) {
                    
                    // Remove first world from the array and put it into a var.
                    String worldName = worldsToBackup.removeFirst();
                    
                    // Copy this world into the backup directory, in a folder called the worlds name.
                    try {
                        FileUtils.copyDirectory(worldName, backupDirName.concat(FILE_SEPARATOR).concat(worldName));
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace(System.out);
                    } catch (IOException e) {
                        LogUtils.sendLog(Level.WARNING, strings.getStringWOPT("errorcreatetemp", worldName), true);
                        /** @TODO create exception classes **/
                        e.printStackTrace(System.out);
                        server.broadcastMessage(strings.getString("backupfailed"));
                    }
                }
            } else {
                LogUtils.sendLog(Level.INFO, strings.getString("skipworlds"), true);
            }
            
            // We are backing up plugins.
            if (BackupPlugins) {
                
                // Copy entire plugins directory to the backup folder.
                FileUtils.copyDirectory("plugins", backupDirName.concat(FILE_SEPARATOR).concat("plugins"));
            } else {
                LogUtils.sendLog(Level.INFO, strings.getString("skipplugins"), true);
            }
            
            // Should we ZIP the backup.
            if (ShouldZIP) {
                
                // Add backup folder to a ZIP.
                FileUtils.zipDir(backupDirName, backupDirName);
                
                // Delete the original backup directory.
                FileUtils.deleteDirectory(new File(backupDirName));
            }
        } else { //Should this be removed, as i do not see why anyone would want this, and it also makes the deleteOldBackups() not very accurate.
            if ((worldsToBackup != null) && (BackupWorlds)) {
                while (!worldsToBackup.isEmpty()) {
                    String worldName = worldsToBackup.removeFirst();
                    String destDir = backupDirName.concat(FILE_SEPARATOR).concat(worldName).concat("-").concat(getFolderName());


                    FileUtils.copyDirectory(worldName, destDir);


                    if (ShouldZIP) {
                        FileUtils.zipDir(destDir, destDir);
                        FileUtils.deleteDirectory(new File(destDir));
                    }
                }
            } else {
                LogUtils.sendLog(strings.getString("skipworlds"));
            }

            if (BackupPlugins) {
                String destDir = backupDirName.concat(FILE_SEPARATOR).concat("plugins").concat("-").concat(getFolderName());
                FileUtils.copyDirectory("plugins", destDir);
                if (ShouldZIP) {
                    FileUtils.zipDir(destDir, destDir);
                    FileUtils.deleteDirectory(new File(destDir));
                }

            } else {
                LogUtils.sendLog(Level.INFO, strings.getString("skipplugins"), true);
            }

        }

        // Delete old backups.
        deleteOldBackups();

        // Clean up.
        finish();
    }
    
    /**
     * Get the name of this backups folder.
     * 
     * @return The name, as a string.
     */
    private String getFolderName() {

        Calendar cal = Calendar.getInstance();
        String formattedDate;

        // Java string (and date) formatting:
        // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax
        try {
            formattedDate = String.format(settings.getStringProperty("dateformat"), cal);
        } catch (Exception e) {
            LogUtils.sendLog(Level.WARNING, strings.getString("errordateformat"), true);
            formattedDate = String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", cal);

            // @TODO write exception class
            System.out.println(e);
        }
        return formattedDate;
    }

   /**
     * Check whether there are more backups as allowed to store. 
     * When this case is true, it deletes oldest ones.
     */
    private void deleteOldBackups() {
        try {
            // Get properties.
            File backupDir = new File(settings.getStringProperty("backuppath"));
            final int maxBackups = settings.getIntProperty("maxbackups");
            
            // Store all backup files in an array.
            File[] filesList = backupDir.listFiles();

            // If the amount of files exceeds the max backups to keep.
            if (filesList.length > maxBackups) {
                ArrayList<File> backupList = new ArrayList<File>(filesList.length);
                backupList.addAll(Arrays.asList(filesList));
                
                int maxModifiedIndex;
                long maxModified;

                //Remove the newst backups from the list.
                for (int i = 0 ; i < maxBackups ; ++i) {
                    maxModifiedIndex = 0;
                    maxModified = backupList.get(0).lastModified();
                    for (int j = 1 ; j < backupList.size() ; ++j) {
                        File currentFile = backupList.get(j);
                        if (currentFile.lastModified() > maxModified) {
                            maxModified = currentFile.lastModified();
                            maxModifiedIndex = j;
                        }
                    }
                    backupList.remove(maxModifiedIndex);
                }
                
                //Inform the user what backups are being deleted.
                System.out.println(strings.getString("removeold"));
                System.out.println(Arrays.toString(backupList.toArray()));
                
                // Finally delete the backups.
                for (File backupToDelete : backupList)
                    backupToDelete.delete();
            }
        } catch (Exception e) {
            //@TODO write exception class
            e.printStackTrace(System.out);
        }
    }

   /**
     * Creates a temporary Runnable that is running on the main thread by the scheduler to prevent thread problems.
     */
    private void finish() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (settings.getBooleanProperty("enableautosave")) {
                    server.dispatchCommand(server.getConsoleSender(), "save-on");
                }

                String completedBackupMessage = strings.getString("backupfinished");
                if (completedBackupMessage != null && !completedBackupMessage.trim().isEmpty()) {
                    server.broadcastMessage(completedBackupMessage);
                }
            }
        };
        
        server.getScheduler().scheduleSyncDelayedTask(plugin, run);
    }
}
