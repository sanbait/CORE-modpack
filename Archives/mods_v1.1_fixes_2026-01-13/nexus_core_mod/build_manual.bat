@echo off
cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Java\jdk-17
echo [1/2] Building mod...
call gradlew.bat clean build -x test
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [2/2] Copying to mods folder...
    copy /Y build\libs\nexus_core-*.jar ..\minecraft\mods\
    echo.
    echo ==========================================
    echo SUCCESS! Mod updated.
    echo ==========================================
    echo Restart Minecraft to see changes.
) else (
    echo.
    echo ==========================================
    echo BUILD FAILED!
    echo ==========================================
    echo Check errors above.
)
pause
