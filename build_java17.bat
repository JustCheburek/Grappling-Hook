@echo off
echo Сборка плагина GrapplingHook с использованием Java 17...

REM Сохраняем текущую переменную JAVA_HOME
set OLD_JAVA_HOME=%JAVA_HOME%

REM Устанавливаем JAVA_HOME на путь к Java 17
set JAVA_HOME=C:\Program Files\Java\jdk-17

echo Используем Java 17 для сборки...
echo JAVA_HOME=%JAVA_HOME%

REM Запускаем сборку
call gradlew clean shadowJar --info

REM Восстанавливаем исходную переменную JAVA_HOME
set JAVA_HOME=%OLD_JAVA_HOME%

if exist build\libs\GrapplingHook-1.8.0.jar (
    echo Сборка завершена успешно. JAR-файл находится в build/libs/GrapplingHook-1.8.0.jar
    
    REM Копируем JAR-файл в более простое имя для удобства
    copy build\libs\GrapplingHook-1.8.0.jar build\libs\GrapplingHook.jar
    echo Создана копия с именем build/libs/GrapplingHook.jar
) else (
    echo Ошибка: JAR-файл не был создан
    exit /b 1
) 