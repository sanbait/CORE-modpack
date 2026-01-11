@echo off
cd /d "%~dp0"
echo ==========================================
echo      PREPARING FILES FOR GIT (CORE)
echo ==========================================
echo.

echo [1/2] Adding files to staging area...
git add docs/
git add .agent/rules/
git add nexus_core_mod/
git add minecraft/kubejs/
git add .gitignore

echo.
echo [2/2] Checking status...
git status

echo.
echo ==========================================
echo                 READY
echo ==========================================
echo If the list above looks green (new files),
echo please type the following command to finish:
echo.
echo git commit -m "feat: initial setup"
echo.
pause
