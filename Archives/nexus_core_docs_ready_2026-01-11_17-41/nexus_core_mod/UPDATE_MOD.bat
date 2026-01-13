@echo off
echo ===================================================
echo   NEXUS CORE MOD - UPDATE SCRIPT
echo ===================================================
echo.
echo 1. Building Mod (this may take a minute)...
call gradlew.bat build --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed! Check errors above.
    pause
    exit /b %errorlevel%
)

echo.
echo 2. Installing to Mods folder...
del /q "..\minecraft\mods\nexus_core-*.jar"
copy /Y "build\libs\nexus_core-*.jar" "..\minecraft\mods\"

echo.
echo ===================================================
echo [SUCCESS] Mod updated! 
echo You can now launch the game in Prism Launcher.
echo ===================================================
pause
