@echo off
REM Travel Schedule Manager Launcher
REM This script runs the application with all dependencies included

echo.
echo ================================================
echo   Travel Schedule Manager
echo ================================================
echo.
echo Starting application...
echo.

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

REM Check if JAR file exists
if not exist "%SCRIPT_DIR%target\TravelScheduleManager.jar" (
    echo ERROR: TravelScheduleManager.jar not found!
    echo.
    echo Please build the project first with: mvn clean package
    echo.
    pause
    exit /b 1
)

REM Run the application (fat JAR with all dependencies included)
cd "%SCRIPT_DIR%"
java -jar "%SCRIPT_DIR%target\TravelScheduleManager.jar"

if errorlevel 1 (
    echo.
    echo ================================================
    echo   Application closed with errors
    echo ================================================
    pause
) else (
    echo.
    echo ================================================
    echo   Application closed successfully
    echo ================================================
)
