@echo off
REM Скрипт сборки для GrapplingHook
REM Автоматически использует Java 17 для сборки

REM Сохраняем текущую переменную JAVA_HOME
set OLD_JAVA_HOME=%JAVA_HOME%

REM Устанавливаем JAVA_HOME на путь к Java 17
set JAVA_HOME=C:\Program Files\Java\jdk-17

echo Используем Java 17 для сборки...

REM Запускаем сборку
call gradlew clean build

REM Восстанавливаем исходную переменную JAVA_HOME
set JAVA_HOME=%OLD_JAVA_HOME%

echo Сборка завершена. JAR-файл находится в build/libs/GrapplingHook.jar 