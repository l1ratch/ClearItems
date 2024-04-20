package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bukkit.entity.Player;
import java.net.URL;
import java.net.URLConnection;

public class ClearItems extends JavaPlugin implements Listener {
    private int clearInterval;
    private boolean highlightEnabled;
    private String notificationPrefix;
    private String notification15s;
    private String notification10s;
    private String notification5s;
    private ChatColor highlightColor;
    private final String githubRepo = "https://api.github.com/repos/l1ratch/ClearItems/releases/latest";
    private String latestVersion;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);

        startItemClearTask();

        // Асинхронно проверяем последнюю версию плагина на GitHub
        new BukkitRunnable() {
            @Override
            public void run() {
                checkLatestVersion();
            }
        }.runTaskAsynchronously(this);

        // Асинхронно оповещаем администратора при входе
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            getServer().getOnlinePlayers().forEach(this::notifyAdmin);
        });
    }


    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        startItemClearTask(event.getWorld());
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        clearInterval = config.getInt("clear_interval", 600);
        highlightEnabled = config.getBoolean("highlight_enabled", true);
        notificationPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("notification_prefix", "&7[ItemClear]"));
        notification15s = ChatColor.translateAlternateColorCodes('&', config.getString("notification_15s", "&eItems will be cleared in 15 seconds!"));
        notification10s = ChatColor.translateAlternateColorCodes('&', config.getString("notification_10s", "&eItems will be cleared in 10 seconds!"));
        notification5s = ChatColor.translateAlternateColorCodes('&', config.getString("notification_5s", "&eItems will be cleared in 5 seconds!"));
        String highlightColorStr = config.getString("highlight_color", "RED");
        try {
            highlightColor = ChatColor.valueOf(highlightColorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid highlight color in config. Defaulting to RED.");
            highlightColor = ChatColor.RED;
        }
    }

    private void startItemClearTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    Bukkit.getScheduler().runTask(ClearItems.this, () -> clearItems(world));
                }
            }
        }.runTaskTimer(this, clearInterval * 20L, clearInterval * 20L);
    }

    private void startItemClearTask(World world) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(ClearItems.this, () -> clearItems(world));
            }
        }.runTaskTimer(this, clearInterval * 20L, clearInterval * 20L);
    }

    private void clearItems(World world) {
        Bukkit.getScheduler().runTask(this, () -> {
            world.getEntitiesByClass(Item.class).forEach(Item::remove);
            int warningTime = clearInterval - 15;
            if (warningTime <= 0) {
                broadcastNotification(notificationPrefix + " " + notification15s);
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastNotification(notificationPrefix + " " + notification15s);
                }
            }.runTaskLaterAsynchronously(this, warningTime * 20L);

            warningTime -= 5;
            if (warningTime <= 0) {
                broadcastNotification(notificationPrefix + " " + notification10s);
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastNotification(notificationPrefix + " " + notification10s);
                }
            }.runTaskLaterAsynchronously(this, warningTime * 20L);

            warningTime -= 5;
            if (warningTime <= 0) {
                broadcastNotification(notificationPrefix + " " + notification5s);
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastNotification(notificationPrefix + " " + notification5s);
                }
            }.runTaskLaterAsynchronously(this, warningTime * 20L);

            if (highlightEnabled) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        highlightItems(world);
                    }
                }.runTaskLaterAsynchronously(this, (clearInterval - 15) * 20L);
            }
        });
    }

    private void broadcastNotification(String message) {
        if (!message.isEmpty()) {
            Bukkit.broadcastMessage(message);
        }
    }

    // Метод для проверки последней версии плагина на GitHub
    private void checkLatestVersion() {
        try {
            URL url = new URL(githubRepo);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            // Парсим JSON и получаем последнюю версию
            latestVersion = response.toString().split("\"tag_name\":\"")[1].split("\",")[0];

            // Оповещаем обновление, если доступна новая версия
            if (isNewVersionAvailable(latestVersion)) {
            getLogger().info(notificationPrefix + ChatColor.GREEN + "A new version of the plugin is available: " + ChatColor.GOLD + latestVersion);
            getLogger().info(notificationPrefix + ChatColor.RED + "Download: https://github.com/l1ratch/ClearItems/releases/latest");
            }

        } catch (IOException e) {
            getLogger().warning(notificationPrefix + ChatColor.RED + "Couldn't check the latest version of the plugin: " + e.getMessage());
        }
    }

    // Метод для оповещения администратора
    private void notifyAdmin(Player player) {
        if (player.isOp()) {
            player.sendMessage(notificationPrefix + ChatColor.RED + "A new version of the plugin is available!" + ChatColor.GOLD + "Download: https://github.com/l1ratch/ClearItems/releases/latest");
        }
    }

    // Метод для сравнения версий и определения, доступна ли новая версия
    private boolean isNewVersionAvailable(String latestVersion) {
        String currentVersion = getDescription().getVersion();
        // Простое сравнение строк версий
        return currentVersion.compareTo(latestVersion) < 0;
    }

    private void highlightItems(World world) {
        for (Item item : world.getEntitiesByClass(Item.class)) {
            item.setCustomNameVisible(true);
            item.setCustomName(highlightColor + item.getItemStack().getType().toString());
        }
    }
}
