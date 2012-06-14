package com.bukkitbackup.lite.threading;

import com.bukkitbackup.lite.config.Settings;
import com.bukkitbackup.lite.utils.FileUtils;
import static com.bukkitbackup.lite.utils.FileUtils.FILE_SEPARATOR;
import com.bukkitbackup.lite.utils.LogUtils;
import com.bukkitbackup.lite.utils.SharedUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BackupTask implements Runnable {

    // instances
    private Server server;
    private Plugin plugin;
    private Settings settings;
    private SyncSaveAll syncSaveAllUtil;
    // settings
    private LinkedList<String> worldsToBackup;
    private List<String> pluginList;
    private boolean pluginListMode;

    private String worldContainer;
    private String backupName; // the backups name, based on date an time. (default: '20120316-091450')
    // folders
    private String backupsFolder; // the root of all out backups (default: 'backups/') with trailing /
    private String tempFolder; // the root of the temp folder (default: 'backups/temp/') with trailing /
    private String thisFinalDestination; // the final resting place for the backup (default: 'backups/20120316-091450')
    private String thisTempDestination; // the temp instance folder (default: 'backups/temp/20120316-091450')

    /**
     * The main BackupTask constructor.
     *
     * @param settings The settings object, to get the plugins settings.
     * @param strings Strings object, for all string values
     * @param worldsToBackup The list of worlds that need to be backed up.
     * @param server The server we are backing up.
     */
    public BackupTask(Server server, Settings settings, LinkedList<String> worldsToBackup) {
        this.server = server;
        this.plugin = server.getPluginManager().getPlugin("BackupLite");
        this.settings = settings;
        this.worldsToBackup = worldsToBackup;
    }

    @Override
    public void run() {
        // Get config.
        pluginListMode = settings.getBooleanProperty("pluginlistmode");
        pluginList = Arrays.asList(settings.getStringProperty("pluginlist").split(";"));

        // Process the backup.
        processBackup();
    }

    /**
     * This method does high-level backup processing.
     */
    public void processBackup() {

        // Container for worlds.
        worldContainer = server.getWorldContainer().getName().concat(FILE_SEPARATOR);

        // This instance name.
        backupName = getFolderName();

        // Where the backups are going.
        backupsFolder = settings.getStringProperty("backuppath").concat(FILE_SEPARATOR);
        thisFinalDestination = backupsFolder.concat(backupName);

        // Temp folder.
        tempFolder = backupsFolder.concat("temp").concat(FILE_SEPARATOR);
        SharedUtils.checkFolderAndCreate(new File(tempFolder));
      
        // This temp instance.
        thisTempDestination = tempFolder.concat(backupName).concat(FILE_SEPARATOR);
        SharedUtils.checkFolderAndCreate(new File(thisTempDestination));
        
        // Do the bakcups.
        backupWorlds();
        backupPlugins();

        // Compress them.
        doCopyAndZIP(thisTempDestination, thisFinalDestination);

        // Do old backup checking.
        if (!deleteOldBackups()) {
            LogUtils.sendLog("Failed to delete old backups.");
        }

        // Complete.
        finishBackup();
    }

    private void backupWorlds() {

        // Loops each world that needs to backed up.
        while (!worldsToBackup.isEmpty()) {

            // Remove first world from the array and put it into a var.
            String loopWorldName = worldsToBackup.removeFirst();

            String worldRootBackupPath = backupsFolder;
            String worldRootTempPath = tempFolder;
            String worldTempDestination = thisTempDestination;

            if (!worldContainer.equals(".")) {
                worldRootBackupPath = backupsFolder.concat(worldContainer);
                worldRootTempPath = tempFolder.concat(worldContainer);
                worldTempDestination = thisTempDestination.concat(worldContainer);
            }

                // This worlds backup folder.
                String loopDestination = worldTempDestination.concat(loopWorldName);

                // Copy the current world into it's backup folder.
                try {
                    FileUtils.copyDirectory(worldContainer.concat(loopWorldName), loopDestination);

                } catch (FileNotFoundException ex) {
                    LogUtils.exceptionLog(ex);
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe);
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

                        String findMe = "plugins".concat(FILE_SEPARATOR).concat(pluginList.get(i));

                        int isFound = name.getPath().indexOf(findMe);

                        // Check if the current plugin matches the string.
                        if (isFound != -1) {

                            // Return false for exclude, true to include.
                            if (pluginListMode) {
                                return false;
                            } else {
                                return true;
                            }
                        }
                    }
                }

                if (pluginListMode) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        // Setup Source and destination DIR's.
        File pluginsFolder = new File("plugins");

        // Touch the folder to update the modified date.
        pluginsFolder.setLastModified(System.currentTimeMillis());

        // Check if this is a split backup or not, and set backup path depending on this.
        String pluginsBackupPath;
        String finalPluginsPath;
        
            pluginsBackupPath = thisTempDestination.concat(FILE_SEPARATOR).concat("plugins");
            finalPluginsPath = null;
        

        // Create if needed.
        SharedUtils.checkFolderAndCreate(new File(pluginsBackupPath));

        // Perform plugin backup.
        try {
            if (pluginList.size() > 0 && !pluginList.get(0).isEmpty()) {
                if (pluginListMode) {
                    LogUtils.sendLog("The following plugins are disabled:");
                } else {
                    LogUtils.sendLog("The following plugins are enabled:");
                }
                LogUtils.sendLog(pluginList.toString());
            }
            FileUtils.copyDirectory(pluginsFolder, new File(pluginsBackupPath), pluginsFileFilter, true);
        } catch (FileNotFoundException ex) {
            LogUtils.exceptionLog(ex);
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe);
        }

    }

    /**
     * Get the name of this backups folder.
     *
     * @return The name, as a string.
     */
    private String getFolderName() {
        return String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", Calendar.getInstance());
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

      
            
                try {
                    FileUtils.zipDir(sourceDIR, finalDIR);
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe, "Failed to ZIP backup: IO Exception.");
                }
            
            try {
                // Delete the original doBackup directory.
                FileUtils.deleteDirectory(new File(sourceDIR));
                new File(sourceDIR).delete();
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe, "Failed to delete temp folder: IO Exception.");
            }
        

    }

    /**
     * Check whether there are more backups as allowed to store. When this case
     * is true, it deletes oldest ones.
     */
    private boolean deleteOldBackups() {

        // Get the doBackup's directory.
        File backupDir = new File(settings.getStringProperty("backuppath"));

       

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
        
        return true;
    }

    private void cleanFolder(File backupDir) throws IOException {

        // Get properties.
        try {
            final int maxBackups = settings.getIntProperty("maxbackups");

            // Store all doBackup files in an array.
            File[] filesList = backupDir.listFiles();

            if (filesList == null) {
                LogUtils.sendLog("Failed to list backup directory.");
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
                LogUtils.sendLog("Removing old backups:");
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
                    syncSaveAllUtil = new SyncSaveAll(server, 2);
                    server.getScheduler().scheduleSyncDelayedTask(plugin, syncSaveAllUtil);
                }

                // Delete the temp directory.
                File tempFile = new File(tempFolder);
                deleteDir(tempFile);

                // Notify that it has completed.
                notifyCompleted();
            }

            private void notifyCompleted() {
                String completedBackupMessage = "Completed Backup.";

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
