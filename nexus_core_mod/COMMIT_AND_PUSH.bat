@echo off
setlocal
echo ==========================================
echo      Nexus Core Mod - Git Auto-Commit
echo ==========================================

cd /d "%~dp0"

echo Checking Git status...
git status

echo.
set /p msg="Enter commit message (default: Update): "
if "%msg%"=="" set msg=Update

echo.
echo Adding all files...
git add .

echo.
echo Committing...
git commit -m "%msg%"

echo.
echo ==========================================
echo Commit Complete. 
echo (Note: Remote push is skipped as no remote is configured yet. 
echo  If you have a repo, run 'git push' manually or add it here)
echo ==========================================
pause
