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
echo.
echo ===================================================
echo   PHASE 2: SHADOW GRID UPDATE
echo ===================================================
echo.
cd ..\shadow_grid_mod\shadow_grid_mod
echo Building Shadow Grid...
call gradlew.bat clean build --no-daemon
if %errorlevel% neq 0 (
    echo [ERROR] Shadow Grid build failed!
    pause
    exit /b %errorlevel%
)
cd ..\..\nexus_core_mod

echo.
echo ===================================================
echo   PHASE 3: DEPLOYMENT
echo ===================================================
echo Removing old jars...
del /q "..\minecraft\mods\nexus_core-*.jar" 2>nul
del /q "..\minecraft\mods\shadow_grid-*.jar" 2>nul

echo Installing new versions...
copy /Y "build\libs\nexus_core-*.jar" "..\minecraft\mods\"
copy /Y "..\shadow_grid_mod\shadow_grid_mod\build\libs\shadow_grid-*.jar" "..\minecraft\mods\"

if %errorlevel% neq 0 (
    echo [ERROR] Copy failed!
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] ALL MODS UPDATED!
echo ===================================================
pause
