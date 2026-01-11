@echo off
echo ===========================================
echo SAFE UPDATE SCRIPT FOR NEXUS CORE
echo ===========================================
echo.
echo 1. Checking environment...
where java
if %errorlevel% neq 0 echo [WARN] Java not found in PATH (might be okay if JAVA_HOME is set)

echo.
echo 2. Running Gradle Build...
echo (This step might take 1-2 minutes. Please wait.)
call gradlew.bat build --no-daemon
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] BUILD FAILED!
    echo Please check the error messages above.
    echo.
    pause
    exit /b
)

echo.
echo 3. Build Successful. Listing build directory:
dir build\libs

echo.
echo 4. Copying to mods folder...
copy /Y "build\libs\nexus_core-*.jar" "..\minecraft\mods\"

echo.
echo ===========================================
echo DONE. You can start the game.
echo ===========================================
pause
