@echo off
echo ===================================================
echo   NEXUS CORE MOD - UPDATE SCRIPT (ROOT)
echo ===================================================
echo.

cd nexus_core_mod

echo 1. Cleaning and Building (Versions 1.1.21+)...
call gradlew.bat clean build --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed! Check errors above.
    pause
    exit /b %errorlevel%
)

cd ..

echo.
echo 2. Removing potential duplicates from Mods folder...
:: Attempt delete with error checking
del /q "minecraft\mods\nexus_core-*.jar" 2>nul
if exist "minecraft\mods\nexus_core-*.jar" (
    echo.
    echo [WARNING] Could not delete old jar!
    echo Is Minecraft running? CLOSE THE GAME and try again.
    pause
    exit /b 1
)

echo.
echo 3. Installing new version...
copy /Y "nexus_core_mod\build\libs\nexus_core-*.jar" "minecraft\mods\"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Copy failed!
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] Mod updated to latest version!
echo ===================================================
pause
