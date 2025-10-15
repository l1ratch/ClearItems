package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class ClearItems extends JavaPlugin implements Listener {
    private int clearInterval;
    private List<WarningTask> warningTasks;
    private String clearMessage;
    private BukkitTask mainTask;

    private class WarningTask {
        private final int timeLeft;
        private final String message;
        private BukkitTask task;

        public WarningTask(int timeLeft, String message) {
            this.timeLeft = timeLeft;
            this.message = message;
        }

        public void schedule() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage(message);
                }
            }.runTaskLater(ClearItems.this, (clearInterval - timeLeft) * 20L);
        }

        public void cancel() {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        clearInterval = getConfig().getInt("clearInterval", 300); // Исправлено на 300 секунд по умолчанию
        warningTasks = new ArrayList<>();
        clearMessage = getConfig().getString("clearMessage", null);

        // Обработка сообщений
        List<String> warningMessages = getConfig().getStringList("warningMessages");
        for (String message : warningMessages) {
            if (message != null && !message.trim().isEmpty()) {
                String[] parts = message.split(",", 2); // Ограничиваем split 2 частями
                if (parts.length == 2) {
                    try {
                        int warningTime = Integer.parseInt(parts[0].trim());
                        String warningText = ChatColor.translateAlternateColorCodes('&', parts[1].trim());
                        warningTasks.add(new WarningTask(warningTime, warningText));
                    } catch (NumberFormatException e) {
                        getLogger().warning("Некорректное время предупреждения: " + parts[0]);
                    }
                }
            }
        }

        if (clearMessage != null) {
            clearMessage = ChatColor.translateAlternateColorCodes('&', clearMessage);
        }

        getServer().getPluginManager().registerEvents(this, this);
        startClearTask();
    }

    private void startClearTask() {
        // Отменяем существующую задачу если есть
        if (mainTask != null) {
            mainTask.cancel();
        }

        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                performClear();
            }
        }.runTaskTimer(this, clearInterval * 20L, clearInterval * 20L);

        // Запускаем предупреждения
        scheduleWarnings();
    }

    private void scheduleWarnings() {
        // Отменяем старые предупреждения
        for (WarningTask task : warningTasks) {
            task.cancel();
        }

        // Запускаем новые
        for (WarningTask task : warningTasks) {
            task.schedule();
        }
    }

    private void performClear() {
        int removedItems = 0;

        // Очищаем все миры
        for (World world : Bukkit.getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                if (item.isOnGround()) {
                    item.remove();
                    removedItems++;
                }
            }
        }

        // Сообщение об очистке
        if (clearMessage != null && !clearMessage.isEmpty()) {
            Bukkit.broadcastMessage(clearMessage);
        }

        getLogger().info("Удалено " + removedItems + " предметов");

        // Перезапускаем предупреждения для следующего цикла
        scheduleWarnings();
    }

    @Override
    public void onDisable() {
        if (mainTask != null) {
            mainTask.cancel();
        }
        for (WarningTask task : warningTasks) {
            task.cancel();
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Можно добавить логику отслеживания предметов
    }
}