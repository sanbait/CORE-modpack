@echo off
cd /d "%~dp0"
echo ==========================================
echo        SAVING PROJECT PROGRESS
echo ==========================================

echo [1/2] Adding all changed files...
git add .

echo.
echo [2/2] Committing changes...
set "timestamp=%date% %time%"
git commit -m "WIP: Progress save at %timestamp%"

echo.
echo ==========================================
echo           PROGRESS SAVED!
echo ==========================================
echo.
pause
