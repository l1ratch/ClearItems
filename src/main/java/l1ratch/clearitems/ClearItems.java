package l1ratch.clearitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClearItems extends JavaPlugin implements Listener {
    private int clearInterval;
    private List<WarningTask> warningTasks;
    private String clearMessage;
    private BukkitTask mainTask;
    private List<String> excludedWorlds;
    private List<String> enabledWorlds;

    private class WarningTask {
        private final int timeLeft;
        private final String message;
        private BukkitTask task;

        public WarningTask(int timeLeft, String message) {
            this.timeLeft = timeLeft;
            this.message = message;
        }

        public void schedule() {
            // Проверяем, что время предупреждения не превышает интервал очистки
            if (timeLeft >= clearInterval) {
                getLogger().warning("Время предупреждения " + timeLeft + " превышает интервал очистки " + clearInterval + ". Предупреждение пропущено.");
                return;
            }

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
        // Загрузка конфигурации
        loadConfig();

        // Регистрация событий и команд
        getServer().getPluginManager().registerEvents(this, this);

        // Запуск задачи очистки
        startClearTask();

        getLogger().info("Плагин ClearItems успешно запущен!");
        logWorldSettings();
    }

    private void loadConfig() {
        // Загрузка и сохранение конфигурации по умолчанию
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        // Загрузка основных настроек
        clearInterval = getConfig().getInt("clearInterval", 300);
        clearMessage = getConfig().getString("clearMessage", null);

        // Загрузка списков миров (регистронезависимые)
        excludedWorlds = getConfig().getStringList("excludedWorlds").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        enabledWorlds = getConfig().getStringList("enabledWorlds").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // Обработка сообщений предупреждений
        warningTasks = new ArrayList<>();
        List<String> warningMessages = getConfig().getStringList("warningMessages");
        for (String message : warningMessages) {
            if (message != null && !message.trim().isEmpty()) {
                String[] parts = message.split(",", 2);
                if (parts.length == 2) {
                    try {
                        int warningTime = Integer.parseInt(parts[0].trim());
                        String warningText = parts[1].trim();

                        // Пропускаем пустые сообщения
                        if (!warningText.isEmpty()) {
                            warningText = ChatColor.translateAlternateColorCodes('&', warningText);
                            warningTasks.add(new WarningTask(warningTime, warningText));
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("Некорректное время предупреждения: " + parts[0]);
                    }
                }
            }
        }

        // Обработка основного сообщения
        if (clearMessage != null && !clearMessage.isEmpty()) {
            clearMessage = ChatColor.translateAlternateColorCodes('&', clearMessage);
        }
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

        // Очищаем миры согласно настройкам
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName().toLowerCase();

            // Проверка включенных миров (приоритет)
            if (!enabledWorlds.isEmpty()) {
                if (!enabledWorlds.contains(worldName)) {
                    continue; // Пропускаем мир, если он не в списке разрешенных
                }
            }
            // Проверка исключенных миров
            else if (excludedWorlds.contains(worldName)) {
                continue; // Пропускаем исключенный мир
            }

            // Очистка предметов в мире
            for (Item item : world.getEntitiesByClass(Item.class)) {
                if (item.isOnGround()) {
                    item.remove();
                    removedItems++;
                }
            }
        }

        // Сообщение о очистке
        if (clearMessage != null && !clearMessage.isEmpty()) {
            Bukkit.broadcastMessage(clearMessage);
        }

        getLogger().info("Удалено " + removedItems + " предметов");

        // Перезапускаем предупреждения для следующего цикла
        scheduleWarnings();
    }

    private void logWorldSettings() {
        if (!enabledWorlds.isEmpty()) {
            getLogger().info("Очистка работает только в мирах: " + String.join(", ", enabledWorlds));
        } else if (!excludedWorlds.isEmpty()) {
            getLogger().info("Исключенные миры: " + String.join(", ", excludedWorlds));
        } else {
            getLogger().info("Очистка работает во всех мирах");
        }
    }

    public boolean isWorldEnabled(String worldName) {
        String searchName = worldName.toLowerCase();

        // Приоритет у включенных миров
        if (!enabledWorlds.isEmpty()) {
            return enabledWorlds.contains(searchName);
        }

        // Если включенные миры не указаны, проверяем исключенные
        return !excludedWorlds.contains(searchName);
    }

    public void reloadPluginConfig() {
        getLogger().info("Перезагрузка конфигурации...");

        // Отменяем текущие задачи
        if (mainTask != null) {
            mainTask.cancel();
        }
        for (WarningTask task : warningTasks) {
            task.cancel();
        }

        // Загружаем новую конфигурацию
        loadConfig();

        // Перезапускаем задачи
        startClearTask();

        getLogger().info("Конфигурация успешно перезагружена!");
        logWorldSettings();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("clearitems")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("clearitems.reload")) {
                    reloadPluginConfig();
                    sender.sendMessage(ChatColor.GREEN + "Конфигурация ClearItems перезагружена!");
                } else {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Использование: /clearitems reload");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // Отменяем все задачи при выключении плагина
        if (mainTask != null) {
            mainTask.cancel();
        }
        for (WarningTask task : warningTasks) {
            task.cancel();
        }

        getLogger().info("Плагин ClearItems выключен");
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Дополнительная логика при спавне предметов может быть добавлена здесь
        World world = event.getEntity().getWorld();
        if (!isWorldEnabled(world.getName())) {
            // Предмет в отключенном мире
        }
    }
}