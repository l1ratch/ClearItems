package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClearItems extends JavaPlugin implements Listener {

    private int clearDelay;
    private String clearMessage;
    private int logClearInterval;
    private boolean enableLogging;
    private String notification5s;
    private String notification10s;
    private String notification15s;
    private boolean highlight15sItems;
    private String highlight15sItems_msg1;
    private String highlight15sItems_msg2;

    private File logFile;
    private BukkitTask highlightTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        setupLogFile();
        startAutoClearTask();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        clearDelay = config.getInt("clear_delay", 60); // Default clear delay: 60 seconds
        clearMessage = config.getString("clear_message", "The dropped item has been cleared.").replaceAll("&", "§"); // Default clear message
        logClearInterval = config.getInt("log_clear_interval", 3600); // Default log clear interval: 1 hour
        enableLogging = config.getBoolean("enable_logging", false); // Default enable logging: false
        notification5s = config.getString("notification_5s", "Item will be cleared in 5 seconds.").replaceAll("&", "§");
        notification10s = config.getString("notification_10s", "Item will be cleared in 10 seconds.").replaceAll("&", "§");
        notification15s = config.getString("notification_15s", "Item will be cleared in 15 seconds.").replaceAll("&", "§");
        highlight15sItems = config.getBoolean("highlight_15s_items", false); // Default highlight 15s items: false
        highlight15sItems_msg1 = config.getString("highlight_15s_Items_msg1", "Item will be cleared in 15 seconds.");
        highlight15sItems_msg2 = config.getString("highlight_15s_Items_msg2", " will be cleared in 15 seconds.");
    }

    private void setupLogFile() {
        logFile = new File(getDataFolder(), "log.txt");
        if (!logFile.exists() && enableLogging) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startAutoClearTask() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::clearLogFile, 0, logClearInterval * 20); // Clear log file every logClearInterval seconds
    }

    private void clearLogFile() {
        if (enableLogging) {
            try {
                FileWriter writer = new FileWriter(logFile);
                writer.write(""); // Clearing the file
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logToFile(String message) {
        if (enableLogging) {
            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(message + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        event.getPlayer().sendMessage(notification15s);
        if (highlight15sItems) {
            highlightItem(event.getItemDrop());
        }
        getServer().getScheduler().runTaskLater(this, () -> {
            event.getPlayer().sendMessage(notification10s);
        }, (clearDelay - 10) * 20); // 10 seconds before clear
        getServer().getScheduler().runTaskLater(this, () -> {
            event.getPlayer().sendMessage(notification5s);
        }, (clearDelay - 5) * 20); // 5 seconds before clear
        getServer().getScheduler().runTaskLater(this, () -> {
            event.getItemDrop().remove();
            event.getPlayer().sendMessage(clearMessage);
            if (enableLogging) {
                logToFile("[ClearItems] " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + event.getPlayer().getName() + " the dropped item has been cleared.");
            }
        }, clearDelay * 20); // Convert seconds to ticks
    }

    private void highlightItem(Item item) {
        highlightTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (item.isValid()) {
                String displayName = item.getItemStack().getItemMeta().getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    item.setCustomName(ChatColor.RED + highlight15sItems_msg1);
                } else {
                    item.setCustomName(ChatColor.RED + displayName + highlight15sItems_msg2);
                }
                item.setCustomNameVisible(true);
            } else {
                highlightTask.cancel();
            }
        }, 0, 10);
    }
}
