@echo off
echo Сборка плагина GrapplingHook для Minecraft 1.21.4...

REM Проверяем наличие Java 17
java -version 2>&1 | findstr "17" > nul
if %ERRORLEVEL% NEQ 0 (
    echo ВНИМАНИЕ: Не обнаружена Java 17. Для корректной сборки требуется Java 17.
    echo Проверьте, что Java 17 установлена и доступна в PATH.
    pause
    exit /b 1
)

REM Очищаем предыдущие сборки
echo Очистка предыдущих сборок...
if exist build\libs rmdir /s /q build\libs
mkdir build\libs 2>nul

REM Запускаем сборку с Gradle
echo Запуск сборки через Gradle...
call gradlew clean shadowJar --stacktrace

REM Проверяем результат сборки
if exist build\libs\GrapplingHook-1.8.0.jar (
    echo Сборка завершена успешно!
    echo JAR-файл создан: build\libs\GrapplingHook-1.8.0.jar
    
    REM Копируем JAR-файл с более простым именем
    copy build\libs\GrapplingHook-1.8.0.jar build\libs\GrapplingHook.jar
    echo Создана копия с именем build\libs\GrapplingHook.jar
    
    echo Плагин готов к использованию на сервере Minecraft 1.21.4
) else (
    echo Ошибка: JAR-файл не был создан
    echo Проверьте сообщения об ошибках выше
    pause
    exit /b 1
)

pause 