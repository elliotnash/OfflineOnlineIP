package offlineonline.offlineonline;

import net.md_5.bungee.api.plugin.Plugin;

import offlineonline.offlineonline.cache.ManageCache;
import offlineonline.offlineonline.util.Msg;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Main extends Plugin {

    private static Main instance;
    private static Configuration config;
    private static Plugin plugin;
    private static ManageCache manageCache;
    private boolean disabled = false;

    public void onEnable() {
        instance = this;
        plugin = this;
        manageCache = new ManageCache();

        // loadConfig config
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadConfig();
        Msg.load();
        getLogger().info("Config initialized");

        //loads domains/ips into cache
        List<String> allowedIPs = config.getStringList("allowedIPs");

        new Thread(() -> {

            LinkedList<List<String>> ips = new LinkedList<>();
            for (String domain : config.getStringList("allowedIPs")){
                String[] splitDomain = domain.split(":");
                List<String> ipUser = new ArrayList<>();
                try {
                    if (splitDomain.length<2) {
                        ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                        ips.add(ipUser);
                    } else {
                        ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                        ipUser.addAll(Arrays.asList(splitDomain).subList(1, splitDomain.length));
                        ips.add(ipUser);
                    }

                } catch (UnknownHostException e) {
                    getLogger().warning("domain "+domain+" does not resolve");
                }
            }
            manageCache.setPlayerCache(ips);

        }).start();

        //initialise les Listener+command
        getProxy().getPluginManager().registerCommand(this, new OOCommand(this));
        getProxy().getPluginManager().registerListener(this, new OOListener(this
                , Msg.NOT_ALLOWED_CRACKED_USER.toString()));
        getLogger().info("loaded");

        //set runnable to update host names
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                new Thread (() -> {
                    LinkedList<List<String>> ips = new LinkedList<>();
                    for (String domain : config.getStringList("allowedIPs")){
                        String[] splitDomain = domain.split(":");
                        List<String> ipUser = new ArrayList<>();
                        try {
                            if (splitDomain.length<2) {
                                ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                                ips.add(ipUser);
                            } else {
                                ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                                ipUser.addAll(Arrays.asList(splitDomain).subList(1, splitDomain.length));
                                ips.add(ipUser);
                            }

                        } catch (UnknownHostException e) {
                            getLogger().warning("domain "+domain+" does not resolve");
                        }
                    }
                    manageCache.setPlayerCache(ips);

                }).start();
            }
        }, 30, 30, TimeUnit.MINUTES);

    }

    public void onDisable() {
        for (ProxiedPlayer p : getProxy().getPlayers()) {
            p.disconnect(TextComponent.fromLegacyText(Msg.PLUGIN_DISABLE_KICK_MSG.toString()));
        }
    }

    public boolean hisDisable() {
        return instance.disabled;
    }

    public void sendConsoleMsg(String string) {
        getLogger().info(string);

    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(
                    getConfig(), new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            config = ConfigurationProvider
                    .getProvider(YamlConfiguration.class).load(
                            new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setDisable(boolean disable) {
        instance.disabled = disable;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static Main getInstance() {
        return instance;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static ManageCache getManageCache() {
        return manageCache;
    }
}
