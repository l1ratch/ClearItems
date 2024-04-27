package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearItems extends JavaPlugin {

    private int clearInterval = 300; // Время в тиках (1 секунда = 20 тиков)
    private String removalMessage = "Item removed!"; // Сообщение о удалении предмета
    private Map<Integer, String> warningMessages = new HashMap<>(); // Оповещения о времени до удаления предметов

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

    private void clearItems() {
        Bukkit.getServer().getWorlds().forEach(world -> {
            Bukkit.getScheduler().runTask(this, () -> {
                for (Item item : world.getEntitiesByClass(Item.class)) {
                    // Отправляем оповещения о времени до удаления предмета
                    int timeLeft = (item.getTicksLived() - clearInterval) / 20; // в секундах
                    if (warningMessages.containsKey(timeLeft)) {
                        String message = warningMessages.get(timeLeft);
                        Bukkit.broadcastMessage(message);
                    }
                    // Удаляем предмет, если он существует более clearInterval тиков
                    if (item.getTicksLived() >= clearInterval) {
                        item.remove();
                        // Отправляем сообщение об удалении предмета
                        Bukkit.broadcastMessage(removalMessage);
                    }
                }
            });
        });
    }
}