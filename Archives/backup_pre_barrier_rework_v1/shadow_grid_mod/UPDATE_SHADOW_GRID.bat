@echo off
echo ===================================================
echo   DEVELOPMENT BUILD SCRIPT: SHADOW GRID
echo ===================================================
echo.
echo [1/3] Cleaning previous builds...
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Gradle Clean Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Building Mod (Java 17)...
call gradlew build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Gradle Build Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Deploying to Mods folder...
copy "build\libs\shadow_grid-1.0.0.jar" "..\minecraft\mods\" /Y
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Copy Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Shadow Grid Mod Installed!
echo Location: minecraft/mods/shadow_grid-1.0.0.jar
echo.
pause
