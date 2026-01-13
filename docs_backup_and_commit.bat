@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ==========================================
echo    DOCS & CONFIG SECURITY PROTOCOL
echo ==========================================

:: 1. BACKUP
set "BACKUP_NAME=nexus_core_docs_ready_%date:~-4%-%date:~3,2%-%date:~0,2%_%time:~0,2%-%time:~3,2%"
set "BACKUP_NAME=%BACKUP_NAME: =0%"
set "BACKUP_DIR=Archives\%BACKUP_NAME%"

echo [1/3] Creating Backup: %BACKUP_DIR%...
mkdir "%BACKUP_DIR%"
xcopy "nexus_core_mod" "%BACKUP_DIR%\nexus_core_mod" /E /I /Q /Y
echo       Backup Complete.

:: 2. GIT ADD
echo.
echo [2/3] Staging Changes (Docs + Config)...
git add nexus_core_mod/README.md
git add nexus_core_mod/src/main/java/com/sanbait/nexuscore/NexusCoreConfig.java
git add CHANGELOG.md

:: 3. GIT COMMIT
echo.
echo [3/3] Committing...
git commit -m "docs: huge update to README and sync config with TechGD"

echo.
echo ==========================================
echo        SUCCESS. PIPELINE SECURED.
echo ==========================================
pause
