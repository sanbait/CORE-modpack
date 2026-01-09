@echo off
echo ==========================================
echo      NEXUS CORE AUTO-DEPLOY SYSTEM
echo ==========================================
echo.
echo [1/3] Building Mod (Gradle)...
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build Failed! Check output above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Cleaning old versions...
set TARGET_DIR=..\minecraft\mods

if not exist "%TARGET_DIR%" (
   echo [ERROR] Mods directory not found at: %TARGET_DIR%
   echo Please check folder structure.
   pause
   exit /b 1
)

del /Q "%TARGET_DIR%\nexus_core-*.jar"
echo Old jars removed.

echo.
echo [3/3] Deploying new version...
rem Copy ONLY the main jar (the one with version number, excluding -plain.jar or -sources.jar if possible)
rem But Gradle produces multiple. We will copy ALL from libs but usually people want just the mod.
rem Let's copy specifically the version one and delete others if dragged.
rem Better approach: Gradle clean wipes libs. We just copy everything from libs, but usually libs has sources too.
rem Let's try to copy only the one that DOES NOT have "-plain" in name if possible.
rem Windows batch is dumb. Let's just copy everything for now, but I will try to filter.
copy "build\libs\nexus_core-1.1.17.jar" "%TARGET_DIR%\"

echo.
echo ==========================================
echo      SUCCESS! MOD UPDATED.
echo ==========================================
echo Now launch Minecraft.
pause
