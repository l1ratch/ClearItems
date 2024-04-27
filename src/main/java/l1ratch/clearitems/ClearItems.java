package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearItems extends JavaPlugin implements Listener {
    private int clearInterval; // Интервал в секундах
    private String clearMessage; // Сообщение в чате

    @Override
    public void onEnable() {
        // Загрузка конфигурации
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Инициализация переменных из конфигурации
        clearInterval = getConfig().getInt("clearInterval", 300); // По умолчанию каждые 5 минут
        clearMessage = getConfig().getString("clearMessage", "Все предметы были удалены.");

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
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', clearMessage));
    }
}

