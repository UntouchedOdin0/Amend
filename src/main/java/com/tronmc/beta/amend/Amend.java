package com.tronmc.beta.amend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Amend extends JavaPlugin {
    @Override
    public void onEnable() {
        Integer configcheck = this.getConfig().getInt("config-version");
        //If the integer in the config is less than the config number (old) then it will rename the old one and create a fresh one.
        if (configcheck < 9) {
            File config = new File(this.getDataFolder(), "config.yml");
            File oldconfig = new File(this.getDataFolder(), "config_1.4.1.yml");
            boolean configFlag = config.renameTo(oldconfig);
            this.saveDefaultConfig();
            if (configFlag) {
                getLogger().warning("New config version! PLEASE CHECK THE CONFIG FOLDER!!");
                //Timeout so user sees message.
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                getLogger().warning("An error occurred creating a new config file.");
                //Timeout so user sees message.
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //If the config is = to the config number (newest) then just leave the config alone.
        } else if (configcheck == 9) {
            File config = new File(this.getDataFolder(), "config.yml");
            if (!config.exists()) {
                getLogger().warning("Hey! Welcome to Amend! CHECK OUT THE CONFIG!");
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.saveDefaultConfig();

            //If the config is greater than the config number (n/a) then warn the user to change the config number to one lower.
        } else if (configcheck > 9) {
            this.saveDefaultConfig();
            FileConfiguration config = this.getConfig();
            getLogger().warning("Woah! Your config version is higher then it is supposed to!");
            getLogger().warning("We recommend that you change the config version to 7 so it automatically updates.");
            //Timeout so user sees message.
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Update Checker that checks our API for the latest using a handy dependency called SpigotUpdateChecker.
        new UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://api.tronmc.com/amend/versions/1.19.2") // A link to a URL that contains the latest version as String
                .setDownloadLink("https://amend.mrtron.dev/download") // You can either use a custom URL or the Spigot Resource ID
                .setNotifyOpsOnJoin(false) // Notify OPs on Join when a new version is found (default)
                .checkNow(); // And check right now
        int pluginId = 16293;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("Amend is on standby, ready for updates on shutdown.");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Started Update Check...");
        String BukkitVersion = Bukkit.getVersion().toString();
        String MCVersion = " (MC: 1.19.2)";
        String editVersion = BukkitVersion.replace(MCVersion, "");
        String simpleversion = editVersion.replaceAll("\\D+","");
        int version = Integer.parseInt(simpleversion);

        //Double Check if the user made any changes that the config will be the correct way by refreshing.
        reloadConfig();
        this.getConfig();
        FileConfiguration config = this.getConfig();
        //Grabs the latest version from purpur API and gets the jar-name from the config. Gets other configurations as well.
        String serverJarName = this.getConfig().getString("jar-name");
        String ServerType = this.getConfig().getString("server-type");
        //Boolean ForcedUpdate = this.getConfig().getBoolean("force-update");
        int start = BukkitVersion.indexOf("MC: ") + 4;
        int end = BukkitVersion.length() - 1;
        if (BukkitVersion.substring(start, end).equals("1.19.2")) {

            if (ServerType.equals("paper")) {
                URLConnection connection = null;
                try {
                    connection = new URL("https://api.tronmc.com/amend/versions/paper/1.19.2").openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    String response = scanner.useDelimiter("\\A").next();
                    int latest = Integer.parseInt(response);
                    String[] pathNames;
                    File ServerJar = new File("../");
                    pathNames = ServerJar.list();
                    Bukkit.getLogger().warning("-------------------------------");
                    Bukkit.getLogger().info("Amend");
                    Bukkit.getLogger().info("Server-Type Selected: " + ServerType.toUpperCase());
                    Bukkit.getLogger().info("Current Version: " + BukkitVersion.substring(11));

                    //If the version is not up-to-date it will grab the latest version and download and replace the file and name it based
                    //on the config.
                    if (version != latest) {
                        Bukkit.getLogger().warning("Version is NOT up to date! Newest PAPER version is " + latest);
                        Bukkit.getLogger().info("Downloading update and applying to " + serverJarName + "...");
                        InputStream in = new URL("https://api.papermc.io/v2/projects/paper/versions/1.19.2/builds/" + response + "/downloads/paper-1.19.2-" + response + ".jar").openStream();
                        Files.copy(in, Paths.get(serverJarName), StandardCopyOption.REPLACE_EXISTING);
                        Bukkit.getLogger().info("Update Completed!");
                        Bukkit.getLogger().warning("-------------------------------");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getLogger().info("Successfully updated and disabled Amend!");
                    } else {
                        Bukkit.getLogger().info("Server is up to date!");
                        Bukkit.getLogger().info("Closing plugin...");
                        Bukkit.getLogger().warning("-------------------------------");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getLogger().info("Successfully disabled Amend!");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (ServerType.equals("purpur")) {
                URLConnection connection = null;
                try {
                    connection = new URL("https://api.purpurmc.org/v2/purpur/1.19.2").openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
                    String allbuilds = jobj.get("builds").toString();
                    JsonObject jsonbuilds = new Gson().fromJson(allbuilds, JsonObject.class);
                    String complatest = jsonbuilds.get("latest").toString();
                    String simpleLatest = complatest.substring(1, 5);
                    int latest = Integer.parseInt(simpleLatest);
                    String[] pathNames;
                    File ServerJar = new File("../");
                    pathNames = ServerJar.list();
                    Bukkit.getLogger().warning("-------------------------------");
                    Bukkit.getLogger().info("Amend");
                    Bukkit.getLogger().info("Server-Type Selected: " + ServerType.toUpperCase());
                    Bukkit.getLogger().info("Current Version: " + BukkitVersion.substring(11));

                    //If the version is not up-to-date it will grab the latest version and download and replace the file and name it based
                    //on the config.
                    if (version != latest) {
                        Bukkit.getLogger().warning("Version is NOT up to date! Newest PURPUR version is " + latest);
                        Bukkit.getLogger().info("Downloading update and applying to " + serverJarName + "...");
                        InputStream in = new URL("https://api.purpurmc.org/v2/purpur/1.19.2/latest/download").openStream();
                        Files.copy(in, Paths.get(serverJarName), StandardCopyOption.REPLACE_EXISTING);
                        Bukkit.getLogger().info("Update Completed!");
                        Bukkit.getLogger().warning("-------------------------------");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getLogger().info("Successfully updated and disabled Amend!");
                    } else {
                        Bukkit.getLogger().info("Server is up to date!");
                        Bukkit.getLogger().info("Closing plugin...");
                        Bukkit.getLogger().warning("-------------------------------");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getLogger().info("Successfully disabled Amend!");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Bukkit.getLogger().warning("-------------------------------");
                Bukkit.getLogger().info("Amend");
                Bukkit.getLogger().info("ERROR: Invalid Server Type! Please check the config.");
                Bukkit.getLogger().info("Current Version: " + BukkitVersion.substring(11));
                Bukkit.getLogger().info("Closing plugin...");
                Bukkit.getLogger().warning("-------------------------------");
                getLogger().info("Successfully disabled Amend!");
            }
        } else {
            Bukkit.getLogger().warning("-------------------------------");
            Bukkit.getLogger().info("Amend");
            Bukkit.getLogger().warning("ERROR: Your server version is older, to prevent accidental updates to the world, amend will shut down.");
            //Bukkit.getLogger().info("If you wish to force update, please check the config.");
            Bukkit.getLogger().info("Current Version: " + BukkitVersion.substring(11));
            Bukkit.getLogger().info("Plugin Version: " + "1.19.2");
            Bukkit.getLogger().info("Closing plugin...");
            Bukkit.getLogger().warning("-------------------------------");
            getLogger().info("Successfully disabled Amend!");
        }





    }
}
