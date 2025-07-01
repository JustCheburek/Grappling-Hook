@echo off
echo Сборка плагина GrapplingHook для Minecraft 1.21+...

REM Проверяем наличие Java 17
java -version 2>&1 | findstr "version \"17" > nul
if %ERRORLEVEL% NEQ 0 (
    echo ВНИМАНИЕ: Требуется Java 17 для сборки.
    echo Текущая версия Java:
    java -version
    echo.
    echo Проверьте переменную JAVA_HOME или путь к Java в системе.
    pause
    exit /b 1
)

REM Запускаем сборку с Gradle
echo Запуск сборки через Gradle...
call gradlew clean shadowJar --no-daemon

REM Проверяем результат сборки
if exist build\libs\GrapplingHook-1.8.0.jar (
    echo.
    echo Сборка завершена успешно!
    echo JAR-файл создан: build\libs\GrapplingHook-1.8.0.jar
    
    REM Копируем JAR-файл с более простым именем
    copy build\libs\GrapplingHook-1.8.0.jar build\libs\GrapplingHook.jar
    echo Создана копия с именем build\libs\GrapplingHook.jar
    
    echo.
    echo Плагин готов к использованию на сервере Minecraft 1.21+
) else (
    echo.
    echo Ошибка: JAR-файл не был создан
    echo Проверьте сообщения об ошибках выше
    pause
    exit /b 1
)

pause 