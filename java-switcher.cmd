@echo off
chcp 65001 > nul
rem Script for switching between Java versions (UTF-8 encoding)
rem No administrator rights required

echo ===== Java Version Switcher =====
echo.

rem Check current settings
echo Current settings:
echo JAVA_HOME: %JAVA_HOME%
java -version 2>nul
echo.
echo Press any key to continue...
pause > nul

echo.
echo Available Java versions:
echo 1. Java 24 (usually C:\Program Files\Java\jdk-24)
echo 2. Custom path

echo.
echo Select Java version (1-2):
set /p JAVA_VERSION=

if "%JAVA_VERSION%"=="1" (
    set JAVA_PATH=C:\Program Files\Java\jdk-24
) else if "%JAVA_VERSION%"=="1" (
    echo.
    echo Enter full path to JDK:
    set /p JAVA_PATH=
) else (
    echo Invalid choice.
    echo.
    echo Press any key to exit...
    pause > nul
    goto :EOF
)

rem Check if path exists
if not exist "%JAVA_PATH%" (
    echo.
    echo WARNING: Specified path does not exist: %JAVA_PATH%
    echo Make sure the path is correct and try again.
    echo.
    echo Press any key to exit...
    pause > nul
    goto :EOF
)

rem Set JAVA_HOME for user
setx JAVA_HOME "%JAVA_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo Failed to set JAVA_HOME.
    echo.
    echo Press any key to exit...
    pause > nul
    goto :EOF
)

rem Update PATH for current session
set JAVA_HOME=%JAVA_PATH%
set PATH=%JAVA_PATH%\bin;%PATH%

echo.
echo JAVA_HOME successfully set to: %JAVA_PATH%

echo.
echo Testing new Java version:
java -version

echo.
echo IMPORTANT: Restart command prompt for changes to fully apply.
echo.

echo Press any key to exit...
pause > nul
