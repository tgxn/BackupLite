package com.bukkitbackup.plugin.config;

import com.bukkitbackup.plugin.utils.LogUtils;
import java.io.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class Settings {
    
    private Plugin plugin;
    private Strings strings;
    private File configurationFile;
    private FileConfiguration fileConfiguration;

    public Settings(Plugin plugin, File configurationFile, Strings strings) {
        this.plugin = plugin;
        this.configurationFile = configurationFile;
        this.strings = strings;

        // Checks if config exists, creates if not.
        checkAndCreate();

        // Load the properties into memory.
        loadProperties();

        // Checks configuration version, notifys the user/log.
        checkConfigVersion(true);
    }

    /**
     * Check that the configuration file exists, and creates it if necessary.
     */
    private void checkAndCreate() {
        try {
            if (!configurationFile.exists()) {
                LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                createDefaultSettings();
            }
        } catch (NullPointerException npe) {
            LogUtils.exceptionLog(npe, "Failed to create default configuration file.");
        } catch (SecurityException se) {
            LogUtils.exceptionLog(se, "Failed to create default configuration file.");
        }
    }

    /**
     * Load the configuration to memory from the configurationFile.
     */
    private void loadProperties() {
        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(configurationFile);
        } catch (InvalidConfigurationException ice) {
            LogUtils.exceptionLog(ice, "Failed to load configuration.");
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe, "Failed to load configuration.");
        }
    }

    /**
     * Checks configuration version, and return true if it requires an update.
     *
     * @return False for no update done, True for update done.
     */
    public boolean checkConfigVersion(boolean notify) {

        boolean needsUpgrade = false;

        // Check configuration is loaded.
        if (fileConfiguration != null) {

            // Get the version information from the file.
            String configVersion = fileConfiguration.getString("version", plugin.getDescription().getVersion());
            String pluginVersion = plugin.getDescription().getVersion();

            // Check we got a version from the config file.
            if (configVersion == null) {
                LogUtils.sendLog(strings.getString("failedtogetpropsver"), Level.SEVERE, true);
                needsUpgrade = true;
            }

            // Check if the config is outdated.
            if (!configVersion.equals(pluginVersion)) {
                needsUpgrade = true;
            }

            // After we have checked the versions, we have determined that we need to update.
            if (needsUpgrade && notify) {
                LogUtils.sendLog(Level.SEVERE, strings.getString("configupdate"));
            }
        }
        return needsUpgrade;
    }

    /**
     * Used to upgrade the configuration file.
     */
    public void doConfigurationUpgrade() {
        LogUtils.sendLog(strings.getString("updatingconf"), true);
        if (configurationFile.exists()) {
            configurationFile.delete();
        }
        createDefaultSettings();
        LogUtils.sendLog(strings.getString("updatingconf"), true);
    }

    /**
     * Load the properties file from the JAR and place it in the backup DIR.
     */
    private void createDefaultSettings() {

        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        String line;
        try {
            // Open a stream to the properties file in the jar, because we can only access over the class loader.
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/resources/config.yml")));
            bWriter = new BufferedWriter(new FileWriter(configurationFile));

            // Copy the content to the configfile location.
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe, "Error opening stream.");
        } finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (bWriter != null) {
                    bWriter.close();
                }
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe, "Error closing stream.");
            }
        }
    }

    /**
     * Gets the value of a integer property.
     *
     * @param property The name of the property.
     * @return The value of the property, defaults to -1.
     */
    public int getIntProperty(String property) {
        return fileConfiguration.getInt(property, -1);
    }

    /**
     * Gets the value of a boolean property.
     *
     * @param property The name of the property.
     * @return The value of the property, defaults to true.
     */
    public boolean getBooleanProperty(String property) {
        return fileConfiguration.getBoolean(property, true);
    }

    /**
     * Gets a value of the string property and make sure it is not null.
     *
     * @param property The name of the property.
     * @return The value of the property.
     */
    public String getStringProperty(String property) {
        return fileConfiguration.getString(property, "");
    }

    /**
     * Method that gets the amount of time between backups. - Checks string for
     * no automatic backup. - Checks for if only number (as minutes). - Checks
     * for properly formatted string. - If unknown amount of time, sets as
     * minutes.
     *
     * @return Amount of time between backups. (In minutes)
     */
    public int getIntervalInMinutes(String forSetting) {
        String settingInterval = getStringProperty(forSetting).trim().toLowerCase();
        // If it is null or set to disable.
        if (settingInterval.equals("-1") || settingInterval == null) {
            return 0;
        }
        // If it is just a number, return minutes.
        if (settingInterval.matches("^[0-9]+$")) {
            return Integer.parseInt(settingInterval);
        } else if (settingInterval.matches("[0-9]+[a-z]")) {
            Pattern timePattern = Pattern.compile("^([0-9]+)[a-z]$");
            Matcher amountTime = timePattern.matcher(settingInterval);
            Pattern letterPattern = Pattern.compile("^[0-9]+([a-z])$");
            Matcher letterTime = letterPattern.matcher(settingInterval);
            if (letterTime.matches() && amountTime.matches()) {
                String letter = letterTime.group(1);
                int time = Integer.parseInt(amountTime.group(1));
                if (letter.equals("m")) {
                    return time;
                } else if (letter.equals("h")) {
                    return time * 60;
                } else if (letter.equals("d")) {
                    return time * 1440;
                } else if (letter.equals("w")) {
                    return time * 10080;
                } else {
                    LogUtils.sendLog(strings.getString("unknowntimeident"));
                    return time;
                }
            } else {
                LogUtils.sendLog(strings.getString("checkbackupinterval"));
                return 0;
            }
        } else {
            LogUtils.sendLog(strings.getString("checkbackupinterval"));
            return 0;
        }
    }
}
