package net.tgxn.bukkit.backup.threading;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import net.tgxn.bukkit.backup.BackupMain;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.utils.FileUtils;
import static net.tgxn.bukkit.backup.utils.FileUtils.FILE_SEPARATOR;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.SharedUtils;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * The Task copies and backups the worlds and delete older backups. This task
 * is only runes once in doBackup and doing all the thread safe options.
 * The PrepareBackupTask and BackupTask are two threads to find a compromise between
 * security and performance.
 *
 * @author Kilian Gaertner, Domenic Horner (gamerx)
 */
public class BackupTask implements Runnable {

    private Server server;
    private Plugin plugin;
    private Settings settings;
    private Strings strings;
    private LinkedList<String> worldsToBackup;
    private List<String> pluginList;
    private boolean splitbackup;
    private boolean shouldZIP;
    private String backupsPath;
    private String thisBackupFolder;
    private boolean backupEverything;

    /**
     * The main doBackup constructor.
     * 
     * @param settings The settings object, to get the plugins settings.
     * @param strings Strings object, for all string values
     * @param worldsToBackup The list of worlds that need to be backed up.
     * @param server The server we are backing up.
     */
    public BackupTask(Server server, Settings settings, Strings strings, LinkedList<String> worldsToBackup) {
        this.server = server;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.settings = settings;
        this.strings = strings;
        this.worldsToBackup = worldsToBackup;
    }

    @Override
    public void run() {
        // Load settings.
        backupsPath = settings.getStringProperty("backuppath").concat(FILE_SEPARATOR);
        thisBackupFolder = backupsPath.concat(getFolderName());
        backupEverything = settings.getBooleanProperty("backupeverything");
        splitbackup = settings.getBooleanProperty("splitbackup");
        shouldZIP = settings.getBooleanProperty("zipbackup");
        pluginList = Arrays.asList(settings.getStringProperty("skipplugins").split(";"));

        // Starts the process.
        doBackup();
    }

    /**
     * This method does high-level backup processes.
     *
     */
    public void doBackup() {

        // We are performing a full server doBackup.
        if (backupEverything) {

            // Setup FileFilter to exclude the backups path.
            FileFilter ff = new FileFilter() {

                /**
                 * Files to accept/deny.
                 */
                @Override
                public boolean accept(File f) {

                    // Disallow server.log and the backuppath.
                    if (f.getName().equals(settings.getStringProperty("backuppath"))) {
                        return false;
                    }

                    if (f.getName().equals("server.log")) {
                        return false;
                    }

                    return true;
                }
            };

            // Setup Source and destination DIR's.
            File srcDIR = new File("./");
            File destDIR = new File(thisBackupFolder);

            // Copy this world into the doBackup directory, in a folder called the worlds name.
            try {

                // Copy the directory.
                FileUtils.copyDirectory(srcDIR, destDIR, ff, true);

                // Perform the zipping action. 
                doZIP(thisBackupFolder);

            } catch (FileNotFoundException fnfe) {
                LogUtils.exceptionLog(fnfe.getStackTrace(), "Failed to copy world: File not found.");
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace(), "Failed to copy world: IO Exception.");
            }
        } else {

            // World backup checking.
            if ((worldsToBackup != null) && (settings.getBooleanProperty("backupworlds"))) {
                backupWorlds();
            } else {
                LogUtils.sendLog(strings.getString("skipworlds"), Level.INFO, true);
            }

            // Plugin backup checking.
            if (settings.getBooleanProperty("backupplugins")) {
                backupPlugins();
            } else {
                LogUtils.sendLog(strings.getString("skipplugins"), Level.INFO, true);
            }

            // Check if split backup.
            if (!splitbackup) {
                doZIP(thisBackupFolder);
            }
        }

        // Should we delete any old backups.
        if (!deleteOldBackups()) {
            LogUtils.sendLog("Failed to delete old backups.");
        }

        // Finish backup.
        finishBackup();
    }

    /**
     * This backs up worlds.
     *
     * No checking if they are needed is required, it is already done.
     *
     */
    private void backupWorlds() {

        // Loops each world that needs to backed up.
        while (!worldsToBackup.isEmpty()) {

            // Remove first world from the array and put it into a var.
            String currentWorldName = worldsToBackup.removeFirst();

            // Check for split backup.
            if (splitbackup) {

                // Check this worlds folder exists.
                File worldBackupFolder = new File(backupsPath.concat(FILE_SEPARATOR).concat(currentWorldName));

                // Create if needed.
                SharedUtils.checkFolderAndCreate(worldBackupFolder);

                // This worlds backup folder.
                String thisWorldBackupFolder = backupsPath.concat(currentWorldName).concat(FILE_SEPARATOR).concat(getFolderName());

                // Copy the current world into it's backup folder.
                try {
                    FileUtils.copyDirectory(currentWorldName, thisWorldBackupFolder);
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.out);
                    LogUtils.sendLog("Failed to copy world: IO Exception.");
                }

                // Check and ZIP folder.
                doZIP(thisWorldBackupFolder);

            } else {

                // This worlds backup folder.
                String thisWorldBackupFolder = thisBackupFolder.concat(FILE_SEPARATOR).concat(currentWorldName);

                // Copy the current world into it's backup folder.
                try {
                    FileUtils.copyDirectory(currentWorldName, thisWorldBackupFolder);

                } catch (FileNotFoundException ex) {
                    LogUtils.exceptionLog(ex.getStackTrace());
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe.getStackTrace());
                }
            }
        }
    }

    /**
     * This backs up plugins.
     *
     * No checking if they are needed is required, it is already done.
     *
     */
    private void backupPlugins() {

        // The FileFilter instance for skipped/enabled plugins.
        FileFilter pluginsFileFilter = new FileFilter() {

            @Override
            public boolean accept(File name) {

                // Check if there are plugins listed.
                if (pluginList.size() > 0 && !pluginList.get(0).isEmpty()) {

                    // Loop each plugin.
                    for (int i = 0; i < pluginList.size(); i++) {

                        // Check if the current plugin matches the string.
                        if (pluginList.get(i).equals(name.getName())) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        // Setup Source and destination DIR's.
        File pluginsFolder = new File("plugins");

        // Touch the folder to update the modified date.
        pluginsFolder.setLastModified(System.currentTimeMillis());

        // Check if this is a split backup or not, and set backup path depending on this.
        String pluginsBackupPath;
        if (splitbackup) {
            pluginsBackupPath = backupsPath.concat("plugins").concat(FILE_SEPARATOR).concat(getFolderName());
        } else {
            pluginsBackupPath = thisBackupFolder.concat(FILE_SEPARATOR).concat("plugins");
        }

        // Create if needed.
        SharedUtils.checkFolderAndCreate(new File(pluginsBackupPath));

        // Perform plugin backup.
        try {
            if (pluginList.size() > 0 && !pluginList.get(0).isEmpty()) {
                LogUtils.sendLog(strings.getString("disabledplugins"));
                LogUtils.sendLog(pluginList.toString());
            }
            FileUtils.copyDirectory(pluginsFolder, new File(pluginsBackupPath), pluginsFileFilter, true);
        } catch (FileNotFoundException ex) {
            LogUtils.exceptionLog(ex.getStackTrace());
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe.getStackTrace());
        }

        // Check if ZIP is required.
        if (splitbackup) {
            doZIP(pluginsBackupPath);
        }
    }

    /**
     * Get the name of this backups folder.
     * 
     * @return The name, as a string.
     */
    private String getFolderName() {

        // Get the calendar, and initalize the date format string.
        Calendar calendar = Calendar.getInstance();
        String formattedDate;

        // Java string (and date) formatting:
        // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax
        try {
            formattedDate = String.format(settings.getStringProperty("dateformat"), calendar);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            formattedDate = String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", calendar);
        }
        return formattedDate;
    }

    /**
     * Add the folder specified to a ZIP file.
     * 
     * @param folderToZIP The folder that needs to be ZIP'ed
     */
    private void doZIP(String folderToZIP) {
        if (shouldZIP) {
            // ZIP the Folder.
            try {
                FileUtils.zipDir(folderToZIP, folderToZIP);
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace(), "Failed to ZIP backup: IO Exception.");
            }
            // Delete the folder.
            try {
                // Delete the original doBackup directory.
                FileUtils.deleteDirectory(new File(folderToZIP));
                new File(folderToZIP).delete();
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace(), "Failed to delete temp folder: IO Exception.");
            }
        }
    }

    /**
     * Check whether there are more backups as allowed to store. 
     * When this case is true, it deletes oldest ones.
     */
    private boolean deleteOldBackups() {

        // Get the doBackup's directory.
        File backupDir = new File(settings.getStringProperty("backuppath"));

        // Check if split doBackup or not.
        if (splitbackup) {
            try {
                // Loop the folders, and clean for each.
                File[] foldersToClean = backupDir.listFiles();
                for (int l = 0; l < foldersToClean.length; l++) {

                    // Make sure we are cleaning a directory.
                    if (foldersToClean[l].isDirectory()) {
                        cleanFolder(foldersToClean[l]);
                    }
                }
            } catch (NullPointerException npe) {
                LogUtils.exceptionLog(npe.getStackTrace());
                return false;
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace());
                return false;
            }

        } else {

            // Clean entire directory.
            try {
                cleanFolder(backupDir);
            } catch (NullPointerException npe) {
                LogUtils.exceptionLog(npe.getStackTrace());
                return false;
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe.getStackTrace());
                return false;
            }
        }
        return true;
    }

    private void cleanFolder(File backupDir) throws IOException {

        // Get properties.
        try {
            final int maxBackups = settings.getIntProperty("maxbackups");

            // Store all doBackup files in an array.
            File[] filesList = backupDir.listFiles();

            if (filesList == null) {
                LogUtils.sendLog(Level.SEVERE, "Failed to list backup directory.");
                return;
            }

            // If the amount of files exceeds the max backups to keep.
            if (filesList.length > maxBackups) {
                ArrayList<File> backupList = new ArrayList<File>(filesList.length);
                backupList.addAll(Arrays.asList(filesList));

                int maxModifiedIndex;
                long maxModified;

                //Remove the newst backups from the list.
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

                // Inform the user what backups are being deleted.
                LogUtils.sendLog(strings.getString("removeold"));
                LogUtils.sendLog(Arrays.toString(backupList.toArray()));

                // Finally delete the backups.
                for (File backupToDelete : backupList) {
                    deleteDir(backupToDelete);
                }
            }
        } catch (SecurityException se) {
            LogUtils.exceptionLog(se.getStackTrace(), "Failed to clean old backups: Security Exception.");
        }
    }

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Creates a temporary Runnable that is running on the main thread by the scheduler to prevent thread problems.
     */
    private void finishBackup() {

        // Create new Runnable instance.
        Runnable run = new Runnable() {

            @Override
            public void run() {

                // Should we enable auto-save again?
                if (settings.getBooleanProperty("enableautosave")) {
                    server.dispatchCommand(server.getConsoleSender(), "save-on");
                }

                // Notify that it has completed.
                notifyCompleted();
            }

            private void notifyCompleted() {
                String completedBackupMessage = strings.getString("backupfinished");

                // Check there is a message.
                if (completedBackupMessage != null && !completedBackupMessage.trim().isEmpty()) {

                    if (settings.getBooleanProperty("notifyallplayers")) {
                        server.broadcastMessage(completedBackupMessage);
                    } else {
                        // Verify Permissions
                        Player[] players = server.getOnlinePlayers();
                        // Loop through all online players.
                        for (int pos = 0; pos < players.length; pos++) {
                            Player currentplayer = players[pos];

                            // If the current player has the right permissions, notify them.
                            if (currentplayer.hasPermission("backup.notify")) {
                                currentplayer.sendMessage(completedBackupMessage);
                            }
                        }
                    }
                }
            }
        };
        server.getScheduler().scheduleSyncDelayedTask(plugin, run);
    }
}
