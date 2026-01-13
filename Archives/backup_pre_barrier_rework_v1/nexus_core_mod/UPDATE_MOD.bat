@echo off
echo ===================================================
echo   NEXUS CORE MOD - UPDATE SCRIPT
echo ===================================================
echo.
echo 1. Building Mod (this may take a minute)...
call gradlew.bat clean build --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed! Check errors above.
    pause
    exit /b %errorlevel%
)

echo.
echo 2. Installing to Mods folder...
echo Using path: ..\minecraft\mods\nexus_core-*.jar

:: Attempt delete
del /q "..\minecraft\mods\nexus_core-*.jar" 2>nul
if exist "..\minecraft\mods\nexus_core-*.jar" (
    echo.
    echo [WARNING] Could not delete old mod jar! 
    echo Is the game running? CLOSE THE GAME and try again.
    pause
    exit /b 1
)

copy /Y "build\libs\nexus_core-*.jar" "..\minecraft\mods\"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Copy failed!
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] Mod updated! 
echo You can now launch the game in Prism Launcher.
echo ===================================================
pause
