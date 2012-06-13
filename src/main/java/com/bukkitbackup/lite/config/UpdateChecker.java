package com.bukkitbackup.lite.config;

import com.bukkitbackup.lite.utils.LogUtils;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class UpdateChecker implements Runnable {

    private File configFile;
    private YamlConfiguration configuration;
    private Plugin plugin;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void run() {
        try {

            // Load the configuration, Get the User ID.
            String uniqueUserID = loadConfigReturnGUID();

            // Check if we should be connecting.
            if (configuration.getBoolean("enablecheck") && (!configuration.getString("uuid").equals(""))) {

                // Do the checkin, return the response.
                String webResponse = getWebResponse(uniqueUserID);

                // Check that we recieved a response.
                if (webResponse == null) {
                    LogUtils.sendLog("Failed to get version information.");
                } else {
                    
                    // Check versions and output log to the user.
                    if (webResponse.equals(plugin.getDescription().getVersion()))
                        LogUtils.sendLog("Up-to-Date");
                    else
                        LogUtils.sendLog("Out of date");
                }
            }
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex, "Failed to load update configuration");
        }
    }

    private String loadConfigReturnGUID() throws IOException {
        configuration = YamlConfiguration.loadConfiguration(configFile);
        configuration.addDefault("enablecheck", true);
        configuration.addDefault("guid", UUID.randomUUID().toString());
        if (configuration.get("guid", null) == null) {
            configuration.options().header("Update Configuration File - http://metrics.bukkitbackup.com").copyDefaults(true);
            configuration.save(configFile);
        }
        return configuration.getString("guid");
    }

    private String getWebResponse(String uniqueUserID) throws IOException {

        StringBuilder data = new StringBuilder();
        data.append(encode("guid")).append('=').append(encode(uniqueUserID));
        encodeDataPair(data, "version", plugin.getDescription().getVersion());
        encodeDataPair(data, "server", plugin.getServer().getVersion());
        encodeDataPair(data, "players", Integer.toString(plugin.getServer().getOnlinePlayers().length));

        // Create the url
        URL url = new URL(String.format("http://metrics.bukkitbackup.com/?", encode(plugin.getDescription().getName())));

        // Connect to the website
        URLConnection connection;

        // Mineshafter creates a socks proxy, so we can safely bypass it
        // It does not reroute POST requests so we need to go around it
        if (isMineshafterPresent()) {
            connection = url.openConnection(Proxy.NO_PROXY);
        } else {
            connection = url.openConnection();
        }

        connection.setDoOutput(true);

        // Write the data
        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data.toString());
        writer.flush();

        // Now read the response
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String response = reader.readLine();

        // close resources
        writer.close();
        reader.close();

        if (response == null || response.startsWith("Error")) {
            throw new IOException(response); //Throw the exception
        }
        return response;
    }

    private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
        buffer.append('&').append(encode(key)).append('=').append(encode(value));
    }

    /**
     * Check if mineshafter is present. If it is, we need to bypass it to send
     * POST requests
     *
     * @return
     */
    private boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Encode text as UTF-8
     *
     * @param text
     * @return
     */
    private static String encode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }
}
