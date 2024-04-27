package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ClearItems extends JavaPlugin implements Listener {
    private int clearInterval; // Интервал в секундах
    private List<String> warningMessages; // Список сообщений с предупреждениями
    private String clearMessage; // Сообщение в чате

    @Override
    public void onEnable() {
        // Загрузка конфигурации
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Инициализация переменных из конфигурации
        clearInterval = getConfig().getInt("clearInterval", 60); // По умолчанию каждые 5 минут
        warningMessages = getConfig().getStringList("warningMessages"); // Сообщения с предупреждениями
        clearMessage = getConfig().getString("clearMessage", "&cClearItems &7&l| &aВсе предметы были удалены.");

        // Преобразование цветов в тексте конфигурации
        for (int i = 0; i < warningMessages.size(); i++) {
            warningMessages.set(i, ChatColor.translateAlternateColorCodes('&', warningMessages.get(i)));
        }
        clearMessage = ChatColor.translateAlternateColorCodes('&', clearMessage);

        // Регистрация слушателя событий
        getServer().getPluginManager().registerEvents(this, this);

        // Запуск задачи очистки предметов по расписанию
        Bukkit.getScheduler().runTaskTimer(this, this::clearItems, clearInterval * 20L, clearInterval * 20L);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Ничего не делаем при спавне предметов
    }

    public void clearItems() {
        // Отправляем предупреждение
        for (String message : warningMessages) {
            String[] parts = message.split(",");
            int warningTime = Integer.parseInt(parts[0]);
            String warningMessage = parts[1];

            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', warningMessage));
                }
            }.runTaskLater(this, (clearInterval - warningTime) * 20L);
        }

        // Запланированная задача на очистку предметов
        new BukkitRunnable() {
            @Override
            public void run() {
                // Итерация по всем предметам в мире
                for (Item item : Bukkit.getWorlds().get(0).getEntitiesByClass(Item.class)) {
                    // Проверка, находится ли предмет на земле
                    if (!item.isOnGround()) {
                        continue; // Если не на земле, пропускаем
                    }

                    // Удаление предмета
                    item.remove();
                }

                // Отправка сообщения в чат
                Bukkit.broadcastMessage(clearMessage);
            }
        }.runTaskLater(this, clearInterval * 20L);
    }
}
