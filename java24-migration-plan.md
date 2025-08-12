# План миграции проекта GrapplingHook на Java 24

## Текущее состояние
- Проект собирается с Java 17
- Используется Gradle 8.7
- Плагин Shadow 8.1.1 не поддерживает Java 24 (class file major version 68)

## Шаги миграции

### Шаг 1: Подготовка инфраструктуры (выполнено)
- ✅ Обновлен Gradle до версии 8.7
- ✅ Настроен Java Toolchain для использования Java 17 при сборке

### Шаг 2: Постепенное обновление кода
1. Проверить и обновить устаревшие API (deprecated) в коде:
   - Файл GrapplingListener.java использует устаревшие API:
     - Обновить использование метаданных (FixedMetadataValue) на PersistentDataContainer
     - Проверить устаревшие методы в PlayerFishEvent и ProjectileHitEvent
   - Исправить непроверенные операции (unchecked operations):
     - Добавить дженерики для коллекций
     - Использовать параметризованные типы для HashMap и ArrayList

2. Модернизировать код:
   - Использовать новые возможности Java:
     - Pattern Matching for instanceof
     - Switch Expressions
     - Records для простых классов данных
     - Text Blocks для многострочных строк
     - Sealed Classes для API
     - Virtual Threads для асинхронных операций

3. Обновить зависимости до версий, совместимых с Java 24:
   - Spigot API
   - bStats

### Шаг 3: Миграция на Java 24
После выхода совместимой версии плагина Shadow или альтернативного решения:

1. Обновить настройки Java в build.gradle:
```gradle
java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
  toolchain {
    languageVersion = JavaLanguageVersion.of(24)
  }
}
```

2. Добавить поддержку функций Java 24 (при необходимости):
```gradle
tasks.withType(JavaCompile).configureEach {
  options.compilerArgs += ['--enable-preview']
}
```

### Шаг 4: Тестирование и проверка совместимости
1. Убедиться, что плагин работает на серверах Bukkit/Spigot с Java 24
2. Проверить совместимость с другими плагинами
3. Провести нагрузочное тестирование

### Шаг 5: Релиз
1. Обновить версию плагина
2. Обновить документацию с указанием требования Java 24
3. Опубликовать новую версию

## Временное решение
До полной поддержки Java 24 плагином Shadow:
- Собирать проект с Java 17
- Разрабатывать с использованием Java 24
- Следить за обновлениями плагина Shadow

## Конкретные улучшения кода

### GrapplingListener.java
```java
// Старый код
if(event.getView().getPlayer() instanceof Player){
    Player player = (Player)event.getView().getPlayer();
    // ...
}

// Новый код с Pattern Matching
if(event.getView().getPlayer() instanceof Player player){
    // Переменная player уже доступна
    // ...
}
```

```java
// Старый код с HashMap
private HashMap<String, HookSettings> hookSettings = new HashMap<>();

// Новый код с дженериками
private Map<String, HookSettings> hookSettings = new HashMap<>();
```

### RecipeLoader.java
```java
// Старый код
String name = config.getString("recipes." + recipeNumber + ".name");

// Новый код с Text Blocks
String recipePath = "recipes." + recipeNumber;
String name = config.getString(recipePath + ".name");
```

## Полезные ссылки
- [Java 24 Release Notes](https://jdk.java.net/24/)
- [Gradle Java Toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
- [Shadow Plugin](https://imperceptiblethoughts.com/shadow/) 