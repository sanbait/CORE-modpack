@echo off
echo ==========================================
echo      NEXUS CORE AUTO-DEPLOY SYSTEM
echo ==========================================
echo.
echo [1/3] Building Mod (Gradle)...
set JAVA_HOME=C:\Program Files\Java\jdk-17
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build Failed! Check output above.
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Cleaning old versions...
set TARGET_DIR=..\minecraft\mods

if not exist "%TARGET_DIR%" (
   echo [ERROR] Mods directory not found at: %TARGET_DIR%
   echo Please check folder structure.
   exit /b 1
)

del /Q "%TARGET_DIR%\nexus_core-*.jar"
echo Old jars removed.

echo.
echo [3/3] Deploying new version...
rem Copying built jar
copy /Y "build\libs\nexus_core-*.jar" "%TARGET_DIR%\"

echo.
echo ==========================================
echo      SUCCESS! MOD UPDATED.
echo ==========================================
echo Now launch Minecraft.
exit /b 0
