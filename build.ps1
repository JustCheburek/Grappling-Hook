# PowerShell скрипт для сборки плагина GrapplingHook

# Параметры
$sourceDir = "src\main\java"
$resourcesDir = "src\main\resources"
$outputDir = "build\classes"
$jarOutputDir = "build\libs"
$jarName = "GrapplingHook.jar"

# Создаем директории для выходных файлов
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
New-Item -ItemType Directory -Force -Path $jarOutputDir | Out-Null

# Компилируем исходные файлы
Write-Host "Компиляция исходных файлов..."

# Проверяем, установлена ли Java
try {
    $javaVersion = javac -version 2>&1
    Write-Host "Используется $javaVersion"
} catch {
    Write-Host "Ошибка: Java не найдена. Убедитесь, что Java установлена и добавлена в PATH."
    exit 1
}

# Компилируем все .java файлы
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse
if ($javaFiles.Count -eq 0) {
    Write-Host "Ошибка: Java файлы не найдены в $sourceDir"
    exit 1
}

# Создаем список файлов для компиляции
$fileList = $javaFiles.FullName -join " "

# Компилируем
$compileCommand = "javac -d $outputDir $fileList"
try {
    Invoke-Expression $compileCommand
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Ошибка при компиляции исходных файлов"
        exit 1
    }
} catch {
    Write-Host "Ошибка при компиляции: $_"
    exit 1
}

# Копируем ресурсы
Write-Host "Копирование ресурсов..."
if (Test-Path $resourcesDir) {
    Get-ChildItem -Path $resourcesDir -Recurse | ForEach-Object {
        if (-not $_.PSIsContainer) {
            $targetPath = $_.FullName -replace [regex]::Escape($resourcesDir), $outputDir
            $targetDir = Split-Path -Parent $targetPath
            if (-not (Test-Path $targetDir)) {
                New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
            }
            Copy-Item $_.FullName -Destination $targetPath -Force
        }
    }
}

# Создаем JAR файл
Write-Host "Создание JAR файла..."
$jarPath = Join-Path $jarOutputDir $jarName
$currentLocation = Get-Location
Set-Location $outputDir
jar -cf $jarPath .
Set-Location $currentLocation

if (Test-Path $jarPath) {
    Write-Host "Сборка завершена успешно. JAR файл создан: $jarPath"
} else {
    Write-Host "Ошибка: JAR файл не был создан"
    exit 1
} 