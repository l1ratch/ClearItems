# ClearItems by l1ratch
A plugin to automatically remove objects that have been lying on the ground <br>
for a certain period of time.

# Config File
#ClearItems by l1ratch. A plugin that removes all lying objects in the worlds after a specified interval.<br>
#Note: If you want to disable a specific alert, leave the line blank: notification_15s: ""<br>
#Plugin Version: 3.1<br>

notification_prefix: "&7[ItemClear]" #Prefix for all plugin alerts.<br>

clear_interval: 600 #The interval in seconds between cleaning items.<br>

notification_15s: "&eItems will be cleared in 15 seconds!" #Notification text 15 seconds before cleaning.<br>
notification_10s: "&eItems will be cleared in 10 seconds!" #Notification text 10 seconds before cleaning.<br>
notification_5s: "&eItems will be cleared in 5 seconds!" #Notification text 5 seconds before cleaning.<br>

highlight_enabled: true #Turns on the backlight function of objects if there is less than 15 seconds left after cleaning.<br>
highlight_color: "RED" #The color of the illumination of objects before cleaning (default is "RED" for red).<br>

#Minecraft has a variety of colors that can be used to highlight text or objects. Here are some of them:<br>
  1.Черный (BLACK)<br>
  2.Темно-синий (DARK_BLUE)<br>
  3.Темно-зеленый (DARK_GREEN)<br>
  4.Темно-бирюзовый (DARK_AQUA)<br>
  5.Темно-красный (DARK_RED)<br>
  6.Темно-фиолетовый (DARK_PURPLE)<br>
  7.Золотой (GOLD)<br>
  8.Серый (GRAY)<br>
  9.Темно-серый (DARK_GRAY)<br>
  10.Синий (BLUE)<br>
  11.Зеленый (GREEN)<br>
  12.Бирюзовый (AQUA)<br>
  13.Красный (RED)<br>
  14.Фиолетовый (LIGHT_PURPLE)<br>
  15.Желтый (YELLOW)<br>
  16.Белый (WHITE)<br>
