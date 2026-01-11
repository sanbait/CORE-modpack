@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ==========================================
echo    DOCS CONSOLIDATION COMMIT
echo ==========================================

:: 1. Add New/Moved Files
echo [1/3] Staging Changes...
git add README.md
git add "docs/TechGD/ROADMAP_LUX_SYSTEM.md"
git add "docs/archive/"
git add ".agent/rules/modpack-vision.md"

:: 2. Remove Old Files (Git needs to know they are gone)
:: Note: 'git add .' would handle this, but we are being safe. 
:: We use git add -u to stage deletions of tracked files.
git add -u "docs/GDD/"

:: 3. Commit
echo.
echo [2/3] Committing...
git commit -m "docs: consolidate vision, archive legacy GDDs, update root README"

:: 4. Push Reminder
echo.
echo [3/3] DONE.
echo ==========================================
echo REMINDER: Run 'push_to_github.bat' now!
echo ==========================================
pause
