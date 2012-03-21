package net.tgxn.bukkit.backup.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import net.tgxn.bukkit.backup.utils.LogUtils;

public class UpdateChecker implements Runnable {

    private String thisVersion;
    private Strings strings;
    private URL updateURL;


    /**
     * This class checks the web site for updates.
     * If one is found, it alerts the user.
     *
     * @param thisVersion The version of the currently loaded plugin.
     * @param strings Instance of the strings loader.
     */
    public UpdateChecker(String thisVersion, Strings strings) {
        this.thisVersion = thisVersion;
        this.strings = strings;
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
            if (!webVersion.equals(thisVersion)) {
                LogUtils.sendLog(strings.getString("pluginoutdate", thisVersion, webVersion));
            } else {
                LogUtils.sendLog(strings.getString("pluginupdate", thisVersion));
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
            updateURL = new URL("http://checkin.bukkitbackup.com/?ver=" + thisVersion + "&fromplugin");

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
