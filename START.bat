@echo off
REM Quick Launcher for Travel Schedule Manager
REM This runs the application using Maven

echo.
echo ================================================
echo   Travel Schedule Manager
echo   Quick Launcher
echo ================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo.
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo Or use the regular launcher: run.bat
    echo.
    pause
    exit /b 1
)

echo Starting application with Maven...
echo Please wait...
echo.

REM Run the application using Maven
mvn javafx:run

echo.
if errorlevel 1 (
    echo ================================================
    echo   Application closed with errors
    echo ================================================
) else (
    echo ================================================
    echo   Application closed successfully  
    echo ================================================
)
pause
