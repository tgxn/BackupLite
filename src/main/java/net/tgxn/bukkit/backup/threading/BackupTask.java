package net.tgxn.bukkit.backup.threading;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.utils.FileUtils;
import static net.tgxn.bukkit.backup.utils.FileUtils.FILE_SEPARATOR;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.SharedUtils;
import net.tgxn.bukkit.backup.utils.SyncSaveAllUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BackupTask implements Runnable {

    // Private veriables passed to class using params.
    private Server server;
    private Plugin plugin;
    private Settings settings;
    private Strings strings;
    private LinkedList<String> worldsToBackup;
    // Settings variables.
    private List<String> pluginList;
    private boolean splitBackup;
    private boolean shouldZIP;
    private boolean backupEverything;
    private String backupsPath;
    private String theFinalDestination;
    private String tempFolder;
    private String tempInstFolder;
    private String backupName;
    private boolean useTempFolder;
    // Save-All handler.
    private SyncSaveAllUtil syncSaveAllUtil;

    /**
     * The main BackupTask constructor.
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
        backupName = getFolderName();
        backupsPath = settings.getStringProperty("backuppath").concat(FILE_SEPARATOR);
        backupEverything = settings.getBooleanProperty("backupeverything");
        splitBackup = settings.getBooleanProperty("splitbackup");
        shouldZIP = settings.getBooleanProperty("zipbackup");
        pluginList = Arrays.asList(settings.getStringProperty("skipplugins").split(";"));
        useTempFolder = settings.getBooleanProperty("usetemp");

        // Set up temp folder.
        tempFolder = backupsPath.concat(settings.getStringProperty("tempfoldername")).concat(FILE_SEPARATOR);
        if (useTempFolder) {
            SharedUtils.checkFolderAndCreate(new File(tempFolder));
        }

        // ZIP on.
        if (shouldZIP) {
            tempInstFolder = tempFolder.concat(backupName);
        } else {
            if (useTempFolder) {
                tempInstFolder = tempFolder.concat(backupName);
            } else {
                tempInstFolder = backupsPath.concat(backupName);
            }
        }
        if (!splitBackup && useTempFolder) {
            SharedUtils.checkFolderAndCreate(new File(tempInstFolder));
        }

        // Set up destination.
        theFinalDestination = backupsPath.concat(backupName);

        // Pass the torch. ;)
        processBackup();
    }

    /**
     * This method does high-level backup processing.
     */
    public void processBackup() {

        // Are we backing all server files or just worlds and plugins?
        if (backupEverything) {
            doEverythingBackup();
        } else {

            // Make sure world backup is enabled and that we have worlds to backup.
            if (settings.getBooleanProperty("backupworlds") && worldsToBackup != null) {
                backupWorlds();
            } else {
                LogUtils.sendLog(strings.getString("skipworlds"), Level.INFO, true);
            }

            // Check plugin backup is enabled
            if (settings.getBooleanProperty("backupplugins")) {
                backupPlugins();
            } else {
                LogUtils.sendLog(strings.getString("skipplugins"), Level.INFO, true);
            }

            // If this is a non-split backup, we need to ZIP the whole thing.
            if (!splitBackup) {
                if (useTempFolder) {
                    doCopyAndZIP(tempInstFolder, theFinalDestination);
                } else {
                    doCopyAndZIP(tempInstFolder, theFinalDestination);
                }


            }
        }

        // Should we delete any old backups.
        if (!deleteOldBackups()) {
            LogUtils.sendLog("Failed to delete old backups.");
        }

        // Finish backup.
        finishBackup();
    }

    private void doEverythingBackup() {

        // Filefiler for excludes.
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
        File destDIR = new File(tempInstFolder);

        // Copy this world into the doBackup directory, in a folder called the worlds name.
        try {

            // Copy the directory.
            FileUtils.copyDirectory(srcDIR, destDIR, ff, true);

            // Perform the zipping action. 
            doCopyAndZIP(tempInstFolder, theFinalDestination);

        } catch (FileNotFoundException fnfe) {
            LogUtils.exceptionLog(fnfe, "Failed to copy world: File not found.");
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe, "Failed to copy world: IO Exception.");
        }
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
            if (splitBackup) {

                // Check this worlds folder exists.
                File worldBackupFolder = new File(backupsPath.concat(currentWorldName));
                SharedUtils.checkFolderAndCreate(worldBackupFolder);

                // This worlds backup folder.
                String thisWorldBackupFolder = tempFolder.concat(currentWorldName).concat(FILE_SEPARATOR).concat(backupName);
                String finalWorldBackupFolder = backupsPath.concat(currentWorldName).concat(FILE_SEPARATOR).concat(backupName);

                // Copy the current world into it's backup folder.
                try {
                    FileUtils.copyDirectory(currentWorldName, thisWorldBackupFolder);
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.out);
                    LogUtils.sendLog("Failed to copy world: IO Exception.");
                }

                // Check and ZIP folder.
                doCopyAndZIP(thisWorldBackupFolder, finalWorldBackupFolder);

            } else {

                // This worlds backup folder.
                String thisWorldBackupFolder = tempInstFolder.concat(FILE_SEPARATOR).concat(currentWorldName);

                // Copy the current world into it's backup folder.
                try {
                    FileUtils.copyDirectory(currentWorldName, thisWorldBackupFolder);

                } catch (FileNotFoundException ex) {
                    LogUtils.exceptionLog(ex);
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe);
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
        String finalPluginsPath;
        if (splitBackup) {
            pluginsBackupPath = tempFolder.concat("plugins").concat(FILE_SEPARATOR).concat(backupName);
            finalPluginsPath = backupsPath.concat("plugins").concat(FILE_SEPARATOR).concat(backupName);
        } else {
            pluginsBackupPath = tempInstFolder.concat(FILE_SEPARATOR).concat("plugins");
            finalPluginsPath = null;
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
            LogUtils.exceptionLog(ex);
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe);
        }

        // Check if ZIP is required.
        if (splitBackup) {
            doCopyAndZIP(pluginsBackupPath, finalPluginsPath);
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
     * @param folderToZIP
     *
     * ZIPENABLED
     *
     * backups/temp/blah -> backups/blah.zip
     *
     * ~~~ OR ~~~
     *
     * backups/temp/blah -> ( backups/blah
     *
     * sourceDIR finalDIR
     *
     */
    /**
     * Copies items from the temp DIR to the main DIR after ZIP if needed. After
     * it has done the required action, it deletes the source folder.
     *
     * @param sourceDIR The source directory. (ex: "backups/temp/xxxxxxxx")
     * @param finalDIR The final destination. (ex: "backups/xxxxxxxx")
     */
    private void doCopyAndZIP(String sourceDIR, String finalDIR) {

        // Sdo we need to do the ZIP?
        if (shouldZIP) {
            // ZIP the Folder.
            try {
                FileUtils.zipDir(sourceDIR, finalDIR);
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe, "Failed to ZIP backup: IO Exception.");
            }
            // Delete the folder.
            try {
                // Delete the original doBackup directory.
                FileUtils.deleteDirectory(new File(sourceDIR));
                new File(sourceDIR).delete();
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe, "Failed to delete temp folder: IO Exception.");
            }
        } else {
            if (useTempFolder) {
                try {
                    FileUtils.copyDirectory(sourceDIR, finalDIR);
                } catch (IOException ex) {
                    Logger.getLogger(BackupTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Delete the folder.
                try {
                    // Delete the original doBackup directory.
                    FileUtils.deleteDirectory(new File(sourceDIR));
                    new File(sourceDIR).delete();
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe, "Failed to delete temp folder: IO Exception.");
                }
            }
        }

    }

    /**
     * Check whether there are more backups as allowed to store. When this case
     * is true, it deletes oldest ones.
     */
    private boolean deleteOldBackups() {

        // Get the doBackup's directory.
        File backupDir = new File(settings.getStringProperty("backuppath"));

        // Check if split doBackup or not.
        if (splitBackup) {
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
                LogUtils.exceptionLog(npe);
                return false;
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe);
                return false;
            }

        } else {

            // Clean entire directory.
            try {
                cleanFolder(backupDir);
            } catch (NullPointerException npe) {
                LogUtils.exceptionLog(npe);
                return false;
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe);
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
            LogUtils.exceptionLog(se, "Failed to clean old backups: Security Exception.");
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
     * Creates a temporary Runnable that is running on the main thread by the
     * scheduler to prevent thread problems.
     */
    private void finishBackup() {

        // Create new Runnable instance.
        Runnable run = new Runnable() {

            @Override
            public void run() {

                // Should we enable auto-save again?
                if (settings.getBooleanProperty("enableautosave")) {
                    syncSaveAllUtil = new SyncSaveAllUtil(server, 2);
                    server.getScheduler().scheduleSyncDelayedTask(plugin, syncSaveAllUtil);
                }

                // Delete the temp directory.
                File tempFile = new File(tempFolder);
                deleteDir(tempFile);

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
