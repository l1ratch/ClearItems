package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearItems extends JavaPlugin {

    private int clearInterval = 300; // Время в тиках (1 секунда = 20 тиков)
    private String removalMessage = "Item removed! test-0.1"; // Сообщение о удалении предмета
    private final Map<Integer, String> warningMessages = new HashMap<>(); // Оповещения о времени до удаления предметов

    @Override
    public void onEnable() {
        // Создаем конфигурационный файл и загружаем его
        saveDefaultConfig();
        loadConfig();

        // Запускаем задачу для очистки предметов
        new BukkitRunnable() {
            @Override
            public void run() {
                clearItems();
            }
        }.runTaskTimer(this, clearInterval, clearInterval);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        clearInterval = config.getInt("clearInterval", clearInterval);
        removalMessage = config.getString("removalMessage", removalMessage);

        // Загружаем оповещения о времени до удаления предметов
        if (config.contains("warningMessages")) {
            List<String> warningMessagesList = config.getStringList("warningMessages");
            for (String message : warningMessagesList) {
                String[] parts = message.split(",", 2);
                if (parts.length == 2) {
                    int timeLeft = Integer.parseInt(parts[0]);
                    String warningMessage = parts[1];
                    warningMessages.put(timeLeft, warningMessage);
                }
            }
        }
    }

    // Метод удаления предметов
    private void clearItems() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (World world : Bukkit.getServer().getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!(entity instanceof Item)) continue;

                    Item item = (Item) entity;

                    // Проверяем, существует ли предмет и не был ли он уже удален
                    if (!item.isValid() || item.isDead()) continue;

                    // Проверяем, есть ли рядом с предметом игроки
                    boolean isNearPlayers = world.getNearbyEntities(item.getLocation(), 1, 1, 1).stream()
                            .anyMatch(e -> e.getType() == EntityType.PLAYER);
                    if (isNearPlayers) continue;

                    int timeLeft = (item.getTicksLived() - clearInterval) / 20; // в секундах
                    if (warningMessages.containsKey(timeLeft)) {
                        String message = warningMessages.get(timeLeft);
                        Bukkit.broadcastMessage(message);
                    }
                    if (item.getTicksLived() >= clearInterval) {
                        item.remove();
                        Bukkit.broadcastMessage(removalMessage);
                    }
                }
            }
        }, 0L, clearInterval * 20L); // Запускаем задачу сразу и затем через каждый указанный интервал времени
    }




}