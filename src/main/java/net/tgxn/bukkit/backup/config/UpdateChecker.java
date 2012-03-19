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
        try {

            // Configure the URL to pull updated from.
            updateURL = new URL("http://checkin.bukkitbackup.com/?ver=" + thisVersion + "&fromplugin");

            // Read from the URL into a BufferedReader.
            BufferedReader bReader = new BufferedReader(new InputStreamReader(updateURL.openStream()));
            
            // Read the line from the BufferedReader.
            String webVersion = bReader.readLine();

            // Close the BufferedReader.
            bReader.close();
            
            // Check versions and output log to the user.
            if (!webVersion.equals(thisVersion)) {
                LogUtils.sendLog(strings.getString("pluginoutdate", thisVersion, webVersion));
            } else {
                LogUtils.sendLog(strings.getString("pluginupdate", thisVersion));
            }
         } catch (MalformedURLException exeption) {
            LogUtils.sendLog("Unable to retrieve latest version information.");
         } catch (IOException exeption) {
            LogUtils.sendLog("Failed to retrieve latest version information.");
        }
    }
}
