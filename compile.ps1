# Создаем директории для скомпилированных классов и ресурсов
New-Item -ItemType Directory -Force -Path "target/classes"
New-Item -ItemType Directory -Force -Path "target/classes/META-INF"

# Копируем ресурсы
Copy-Item -Path "src/main/resources/*" -Destination "target/classes" -Recurse -Force

# Создаем файл MANIFEST.MF
@"
Manifest-Version: 1.0
Built-By: PowerShell Script
Build-Jdk: 1.8
Created-By: PowerShell Script
"@ | Out-File -FilePath "target/classes/META-INF/MANIFEST.MF" -Encoding utf8

# Компилируем исходные файлы
$sourceFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
$classpath = "."

# Находим JAR-файлы Spigot API и других зависимостей
$userProfile = $env:USERPROFILE
$m2Repo = Join-Path -Path $userProfile -ChildPath ".m2\repository"

# Проверяем наличие Spigot API
$spigotJar = Get-ChildItem -Path $m2Repo -Filter "spigot-api-*.jar" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName

if ($spigotJar) {
    $classpath += ";$spigotJar"
    Write-Host "Найден Spigot API: $spigotJar"
} else {
    Write-Host "Spigot API не найден в локальном репозитории Maven."
}

# Компилируем
javac -d "target/classes" -cp $classpath $sourceFiles

# Создаем JAR-файл
jar -cf "target/GrapplingHook-1.7.0.jar" -C "target/classes" .

Write-Host "Компиляция завершена. JAR-файл создан в target/GrapplingHook-1.7.0.jar" 