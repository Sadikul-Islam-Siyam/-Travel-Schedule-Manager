@echo off
echo Stopping existing Java processes...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM javaw.exe /T 2>nul
timeout /t 2 /nobreak >nul

echo Starting application...
call START.bat
