# Скрипт сборки для GrapplingHook
# Автоматически использует Java 17 для сборки

# Сохраняем текущую переменную JAVA_HOME
$oldJavaHome = $env:JAVA_HOME

# Устанавливаем JAVA_HOME на путь к Java 17
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

Write-Host "Используем Java 17 для сборки..." -ForegroundColor Green

# Запускаем сборку
./gradlew clean build

# Восстанавливаем исходную переменную JAVA_HOME
$env:JAVA_HOME = $oldJavaHome

Write-Host "Сборка завершена. JAR-файл находится в build/libs/GrapplingHook.jar" -ForegroundColor Green 