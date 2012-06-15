package com.bukkitbackup.lite.config;

import com.bukkitbackup.lite.utils.FileUtils;
import com.bukkitbackup.lite.utils.LogUtils;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Settings {
    
    private File configurationFile;
    private FileConfiguration yamlConfig;

    public Settings(File configurationFile) {
        this.configurationFile = configurationFile;

        // Checks if config exists, creates if not.
        checkAndCreateConfig();

        // Load the properties into memory.
        loadProperties();

        // Create backups folder.
        if (FileUtils.checkFolderAndCreate(new File(this.getStringProperty("backuppath"))))
            LogUtils.sendLog("Created backup directory.");
    }

    /**
     * Check that the configuration file exists, and creates it if necessary.
     */
    private void checkAndCreateConfig() {
        try {
            if (!configurationFile.exists())
                createDefaultSettings();
        } catch (Exception ex) {
            LogUtils.exceptionLog(ex, "Failed to create default configuration file.");
        }
    }

    /**
     * Load the configuration to memory from the configurationFile.
     */
    private void loadProperties() {
        try {
            yamlConfig = new YamlConfiguration();
            yamlConfig.load(configurationFile);
        } catch (Exception ex) {
            LogUtils.exceptionLog(ex, "Failed to load configuration.");
        }
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
            LogUtils.sendLog("Generated new configuration file.");
        } catch (Exception ex) {
            LogUtils.exceptionLog(ex, "Error creating configuration.");
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
        return yamlConfig.getInt(property, -1);
    }

    /**
     * Gets the value of a boolean property.
     *
     * @param property The name of the property.
     * @return The value of the property, defaults to true.
     */
    public boolean getBooleanProperty(String property) {
        return yamlConfig.getBoolean(property, true);
    }

    /**
     * Gets a value of the string property and make sure it is not null.
     *
     * @param property The name of the property.
     * @return The value of the property.
     */
    public String getStringProperty(String property) {
        return yamlConfig.getString(property, "");
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
                    LogUtils.sendLog("Error formatting time.");
                    return time;
                }
            } else {
                LogUtils.sendLog("Error formatting time.");
                return 0;
            }
        } else {
            LogUtils.sendLog("Error formatting time.");
            return 0;
        }
    }
}
