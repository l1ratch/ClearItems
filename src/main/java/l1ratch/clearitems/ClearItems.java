package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ClearItems extends JavaPlugin {

    private int clearInterval = 300; // Время в тиках (1 секунда = 20 тиков)
    private String removalMessage = "Item removed!"; // Сообщение о удалении предмета

    @Override
    public void onEnable() {
        // Создаем конфигурационный файл и загружаем его
        saveDefaultConfig();
        loadConfig();

        // Запускаем асинхронную задачу для очистки предметов
        new BukkitRunnable() {
            @Override
            public void run() {
                clearItems();
            }
        }.runTaskTimerAsynchronously(this, clearInterval, clearInterval);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        clearInterval = config.getInt("clearInterval", clearInterval);
        removalMessage = config.getString("removalMessage", removalMessage);
    }

    private void clearItems() {
        Bukkit.getServer().getWorlds().forEach(world -> {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                // Удаляем предмет, если он существует более clearInterval тиков
                if (item.getTicksLived() >= clearInterval) {
                    item.remove();
                    // Отправляем сообщение об удалении предмета
                    Bukkit.broadcastMessage(removalMessage);
                }
            }
        });
    }
}

