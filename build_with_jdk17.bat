@echo off
echo Сборка плагина GrapplingHook для Minecraft 1.21+ с Java 17...

REM Устанавливаем путь к JDK 17
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Используем Java:
"%JAVA_HOME%\bin\java" -version

REM Запускаем сборку с Gradle
echo Запуск сборки через Gradle...
call gradlew clean shadowJar --no-daemon

REM Проверяем результат сборки
if exist build\libs\GrapplingHook.jar (
    echo.
    echo Сборка завершена успешно!
    echo JAR-файл создан: build\libs\GrapplingHook.jar
    echo.
    echo Плагин готов к использованию на сервере Minecraft 1.21+
) else (
    echo.
    echo Проверка наличия JAR-файла с версией...
    dir build\libs
    if exist build\libs\*.jar (
        echo.
        echo Сборка завершена успешно! JAR-файл создан.
        echo Копирую файл для удобства использования...
        copy build\libs\*.jar build\libs\GrapplingHook.jar
        echo Плагин готов к использованию на сервере Minecraft 1.21+
    ) else (
        echo.
        echo Ошибка: JAR-файл не был создан
        echo Проверьте сообщения об ошибках выше
        pause
        exit /b 1
    )
)

pause 