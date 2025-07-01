@echo off
echo Сборка плагина GrapplingHook...

REM Создаем директории для выходных файлов
mkdir build\classes 2>nul
mkdir build\libs 2>nul

REM Компилируем исходные файлы
echo Компиляция исходных файлов...
javac -d build\classes src\main\java\com\snowgears\grapplinghook\*.java src\main\java\com\snowgears\grapplinghook\api\*.java src\main\java\com\snowgears\grapplinghook\utils\*.java

if %ERRORLEVEL% neq 0 (
    echo Ошибка при компиляции исходных файлов
    exit /b 1
)

REM Копируем ресурсы
echo Копирование ресурсов...
xcopy /s /y src\main\resources\*.* build\classes\ 2>nul

REM Создаем JAR файл
echo Создание JAR файла...
cd build\classes
jar -cf ..\libs\GrapplingHook.jar .
cd ..\..

if exist build\libs\GrapplingHook.jar (
    echo Сборка завершена успешно. JAR файл создан: build\libs\GrapplingHook.jar
) else (
    echo Ошибка: JAR файл не был создан
    exit /b 1
) 