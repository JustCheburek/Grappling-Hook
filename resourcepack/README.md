# Ресурспак для плагина Grappling Hook

Этот ресурспак добавляет кастомные текстуры для различных типов крюков из плагина Grappling Hook.

## Установка

1. Скопируйте все файлы из папки `resourcepack` в ZIP-архив
2. Поместите этот ZIP-архив в папку `.minecraft/resourcepacks` вашего клиента Minecraft
3. Активируйте ресурспак в настройках игры (Options -> Resource Packs)

## Особенности

- Каждый тип крюка имеет уникальную текстуру
- Текстуры меняются в зависимости от состояния крюка (заброшен или нет)
- Поддерживаются следующие типы крюков:
  - Обычный крюк (grappling_hook)
  - Мультипулл крюк (multipull_hook)
  - Верёвочный крюк (rope_hook)
  - Деревянный крюк (wood_hook)
  - Каменный крюк (stone_hook)
  - Железный крюк (iron_hook)
  - Золотой крюк (gold_hook)
  - Изумрудный крюк (emerald_hook)
  - Алмазный крюк (diamond_hook)
  - Водный крюк (water_hook)

## Структура файлов

- `assets/minecraft/models/item/fishing_rod.json` - основной файл с предикатами для всех типов крюков
- `assets/minecraft/models/item/<тип_крюка>_uncast.json` - модели для крюков в неиспользуемом состоянии
- `assets/minecraft/models/item/<тип_крюка>_cast.json` - модели для крюков в используемом состоянии
- `assets/minecraft/textures/item/custom/<тип_крюка>1.png` - текстуры для крюков в неиспользуемом состоянии
- `assets/minecraft/textures/item/custom/<тип_крюка>2.png` - текстуры для крюков в используемом состоянии

## Настройка в плагине

Убедитесь, что в файле `config.yml` плагина включена опция `custom_models.enabled: true` и настроены правильные значения CustomModelData для каждого типа крюка:

```yaml
custom_models:
  # Standard hooks
  grappling_hook:
    uncast: 10001
    cast: 11001
  multipull_hook:
    uncast: 10002
    cast: 11002
  rope_hook:
    uncast: 10003
    cast: 11003
  
  # Material hooks
  wood_hook:
    uncast: 10101
    cast: 11101
  stone_hook:
    uncast: 10102
    cast: 11102
  iron_hook:
    uncast: 10103
    cast: 11103
  gold_hook:
    uncast: 10104
    cast: 11104
  emerald_hook:
    uncast: 10105
    cast: 11105
  diamond_hook:
    uncast: 10106
    cast: 11106
  water_hook:
    uncast: 10108
    cast: 11108
  
  enabled: true
``` 