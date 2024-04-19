# ClearItems by l1ratch
A plugin to automatically remove objects that have been lying on the ground <br>
for a certain period of time.

# Plugin Version: 3.1
The plugin code has been changed, bugs have been fixed, and a version control system has been added.

# Config File
#ClearItems by l1ratch. A plugin that removes all lying objects in the worlds after a specified interval.
#Note: If you want to disable a specific alert, leave the line blank: notification_15s: ""
#Plugin Version: 3.1

notification_prefix: "&7[ItemClear]" #Prefix for all plugin alerts.

clear_interval: 600 #The interval in seconds between cleaning items.

notification_15s: "&eItems will be cleared in 15 seconds!" #Notification text 15 seconds before cleaning.
notification_10s: "&eItems will be cleared in 10 seconds!" #Notification text 10 seconds before cleaning.
notification_5s: "&eItems will be cleared in 5 seconds!" #Notification text 5 seconds before cleaning.

highlight_enabled: true #Turns on the backlight function of objects if there is less than 15 seconds left after cleaning.
highlight_color: "RED" #The color of the illumination of objects before cleaning (default is "RED" for red).

#Minecraft has a variety of colors that can be used to highlight text or objects. Here are some of them:
  1.Черный (BLACK)
  2.Темно-синий (DARK_BLUE)
  3.Темно-зеленый (DARK_GREEN)
  4.Темно-бирюзовый (DARK_AQUA)
  5.Темно-красный (DARK_RED)
  6.Темно-фиолетовый (DARK_PURPLE)
  7.Золотой (GOLD)
  8.Серый (GRAY)
  9.Темно-серый (DARK_GRAY)
  10.Синий (BLUE)
  11.Зеленый (GREEN)
  12.Бирюзовый (AQUA)
  13.Красный (RED)
  14.Фиолетовый (LIGHT_PURPLE)
  15.Желтый (YELLOW)
  16.Белый (WHITE)
