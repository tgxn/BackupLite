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

package de.luricos.bukkit.backup.threading;

import de.luricos.bukkit.backup.config.Properties;
import de.luricos.bukkit.backup.config.Strings;
import de.luricos.bukkit.backup.lib.io.FileUtils;
import de.luricos.bukkit.backup.utils.BackupLogger;
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

import static de.luricos.bukkit.backup.lib.io.FileUtils.FILE_SEPARATOR;

/**
 * The Task copies and backups the worlds and delete older backups. This task
 * is only runes once in backup and doing all the thread safe options.
 * The PrepareBackupTask and BackupTask are two threads to find a compromise between
 * security and performance.
 *
 * @author Kilian Gaertner
 */
public class BackupTask implements Runnable {

    private final Properties properties;
    private Strings strings;
    private Plugin plugin;
    private final LinkedList<String> worldsToBackup;
    private final Server server;
    private final String backupName;

    public BackupTask(Properties properties, LinkedList<String> worldsToBackup, Server server, String backupName) {
        this.properties = properties;
        this.worldsToBackup = worldsToBackup;
        this.server = server;
        this.backupName = backupName;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = new Strings(plugin);
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

    // Do the backup
    public void backup() throws Exception {
        // Backup directory name
        String backupDirName = properties.getStringProperty("backuppath").concat(FILE_SEPARATOR);

        // Prefs
        boolean ShouldZIP = properties.getBooleanProperty("zipbackup");
        boolean BackupWorlds = properties.getBooleanProperty("backupworlds");
        boolean BackupPlugins = properties.getBooleanProperty("backupplugins");

        // Store backups in one container
        if (properties.getBooleanProperty("singlebackup")) {
            backupDirName = backupDirName.concat(getDate());

            //get Bakcup DIR and create if does not exist.
            File backupDir = new File(backupDirName);
            if (!backupDir.exists()) {
                //@TODO create try catch exception class on error
                backupDir.mkdir();
            }

            if ((worldsToBackup != null) && (BackupWorlds)) {
                while (!worldsToBackup.isEmpty()) {
                    String worldName = worldsToBackup.removeFirst();
                    try {
                        FileUtils.copyDirectory(worldName, backupDirName.concat(FILE_SEPARATOR).concat(worldName));
                    } catch (FileNotFoundException ex) {

                    } catch (IOException e) {
                        BackupLogger.prettyLog(Level.WARNING, false, strings.getStringWOPT("errorcreatetemp", worldName));
                        /** @TODO create exception classes **/
                        e.printStackTrace(System.out);
                        server.broadcastMessage(strings.getString("backupfailed"));
                    }
                }
            } else {
                BackupLogger.prettyLog(Level.INFO, false, strings.getString("skipworlds"));
            }

            if (BackupPlugins) {
                FileUtils.copyDirectory("plugins", backupDirName.concat(FILE_SEPARATOR).concat("plugins"));
            } else {
                BackupLogger.prettyLog(Level.INFO, false, strings.getString("skipplugins"));
            }

            if (ShouldZIP) {
                FileUtils.zipDir(backupDirName, backupDirName);
                FileUtils.deleteDirectory(backupDir);
            }
        } else { //single backup
            if ((worldsToBackup != null) && (BackupWorlds)) {
                while (!worldsToBackup.isEmpty()) {
                    String worldName = worldsToBackup.removeFirst();
                    String destDir = backupDirName.concat(FILE_SEPARATOR).concat(worldName).concat("-").concat(getDate());


                    FileUtils.copyDirectory(worldName, destDir);


                    if (ShouldZIP) {
                        FileUtils.zipDir(destDir, destDir);
                        FileUtils.deleteDirectory(new File(destDir));
                    }
                }
            } else {
                BackupLogger.prettyLog(strings.getString("skipworlds"));
            }

            if (BackupPlugins) {
                String destDir = backupDirName.concat(FILE_SEPARATOR).concat("plugins").concat("-").concat(getDate());
                FileUtils.copyDirectory("plugins", destDir);
                if (ShouldZIP) {
                    FileUtils.zipDir(destDir, destDir);
                    FileUtils.deleteDirectory(new File(destDir));
                }

            } else {
                BackupLogger.prettyLog(Level.INFO, false, strings.getString("skipplugins"));
            }

        }

        /** Delete old backups **/
        deleteOldBackups();

        finish();
    }

    /**
     * @return String representing the current Date in configured format
     */
    private String getDate() {

        Calendar cal = Calendar.getInstance();
        String formattedDate;

        // Java string (and date) formatting:
        // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax
        try {
            formattedDate = String.format(properties.getStringProperty("dateformat"), cal);
        } catch (Exception e) {
            BackupLogger.prettyLog(Level.WARNING, false, strings.getString("errordateformat"));
            formattedDate = String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", cal);

            // @TODO write exception class
            System.out.println(e);
        }
        return formattedDate;
    }

    /**
     * Check wether there are more backups as allowed to store. When this case
     * is true, it deletes oldest ones
     */
    private void deleteOldBackups() {
        try {
            File backupDir = new File(properties.getStringProperty("backuppath"));

            // currently listed files in backupDir;
            // weird stuff oO
            File[] tmpFiles = backupDir.listFiles();
            File[] files = new File[tmpFiles.length - 1];
            for (int i = 0, j = 0; i < tmpFiles.length - 1; ++i) {
                files[j++] = tmpFiles[i];
            }

            tmpFiles = files;

            final int maxBackups = properties.getIntProperty("maxbackups");

            // When there are more than the max.
            if (tmpFiles.length > maxBackups) {
                // Store the "to delete backup" in a list
                ArrayList<File> backupList = new ArrayList<File>(tmpFiles.length);

                // Add all backups in the list and remove the newest later
                backupList.addAll(Arrays.asList(tmpFiles));

                // the current index of the newest backup
                int maxModifiedIndex;
                // the current time of the newest backup
                long maxModified;

                //remove all newest backupList from the list to delete
                for (int i = 0; i < maxBackups; ++i) {
                    maxModifiedIndex = 0;
                    maxModified = backupList.get(0).lastModified();
                    for (int j = 1; j < backupList.size(); ++j) {
                        File currentFile = backupList.get(j);
                        if (currentFile.lastModified() > maxModified) {
                            maxModified = currentFile.lastModified();
                            maxModifiedIndex = j;
                        }
                    }

                    backupList.remove(maxModifiedIndex);
                }

                BackupLogger.prettyLog(strings.getString("removeold"));
                BackupLogger.prettyLog(Arrays.toString(backupList.toArray()));

                // this are the oldest backups, so delete them
                for (File backupToDelete : backupList) {
                //@TODO create try catch exception class on error
                    backupToDelete.delete();
                }
            }
        } catch (Exception e) {
            //@TODO write exception class
            e.printStackTrace(System.out);
        }
    }

    /**
     * Creates a temporary Runnable that is running on the main thread by the
     * scheduler to prevent thread problems.
     */
    private void finish() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (properties.getBooleanProperty("enableautosave"))
                    server.dispatchCommand(new ConsoleCommandSender(server), "save-on");

                String completedBackupMessage = strings.getString("backupfinished");
                if (completedBackupMessage != null && !completedBackupMessage.trim().isEmpty()) {
                    server.broadcastMessage(completedBackupMessage);
                }
            }
        };

        server.getScheduler().scheduleSyncDelayedTask(plugin, run);
    }
}