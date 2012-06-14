package com.bukkitbackup.lite.config;

import com.bukkitbackup.lite.utils.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.plugin.PluginDescriptionFile;

public class UpdateChecker implements Runnable {

    private PluginDescriptionFile descriptionFile;
    private String clientID;

    /**
     * This class checks the web site for updates.
     * If one is found, it alerts the user.
     *
     * @param descriptionFile The plugin's description.
     * @param clientID
     */
    public UpdateChecker(PluginDescriptionFile descriptionFile, String clientID) {
        this.descriptionFile = descriptionFile;
        this.clientID = clientID;
    }

    /**
     * The run method checks the web site, and provides user feedback.
     */
    public void run() {

        // Read the version.
        String webVersion = getVersion();

        if(webVersion == null) {
            LogUtils.sendLog("Failed to retrieve latest version information.");
        } else {
            // Check versions and output log to the user.
            if (!webVersion.equals(descriptionFile.getVersion())) {
                LogUtils.sendLog("Your version is out of date, Latest: " + webVersion);
            } else {
                LogUtils.sendLog("Your version is correct: " + webVersion);
            }
        }
    }

    /**
     * Opens a BufferedReader and attempts to retrieve the latest version.
     * If this fails, it returns null.
     *
     * @return The latest version, or null if this fails.
     */
    public String getVersion() {
        String webVersion;
        try {

            // Configure the URL to pull updated from.
            URL updateURL = new URL("http://checkin.bukkitbackup.com/?ver=" + descriptionFile.getVersion() + "&guid="+clientID+"&name="+descriptionFile.getName()+"&fromplugin");

            // Read from the URL into a BufferedReader.
            BufferedReader bReader = new BufferedReader(new InputStreamReader(updateURL.openStream()));

            // Read the line from the BufferedReader.
            webVersion = bReader.readLine();

            // Close the BufferedReader.
            bReader.close();

            // Return the version.
            return webVersion;
         } catch (MalformedURLException exeption) {
            return null;
         } catch (IOException exeption) {
            return null;
         }
    }
}
