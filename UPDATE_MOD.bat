@echo off
cd nexus_core_mod
call gradlew build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo 2. Removing old versions...
del /F /Q "..\minecraft\mods\nexus_core-*.jar"

echo 3. Copying Jar to Mods folder...
copy /Y "build\libs\nexus_core-*.jar" "..\minecraft\mods\"nexus_core-1.1.19.jar"

echo ===================================================
echo [SUCCESS] Mod updated! You can now launch the game.
echo ===================================================
pause
