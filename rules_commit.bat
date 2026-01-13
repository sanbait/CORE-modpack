@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ==========================================
echo    RULES CONSOLIDATION COMMIT
echo ==========================================

:: 1. Add New/Updated Files
echo [1/3] Staging Changes...
:: We use force add (-f) because .agent/rules might be ignored by .gitignore
git add -f ".agent/rules/Main Rules.md"
git add -f ".agent/rules/KubeJS_Manual.md"

:: 2. Remove Deleted Files
git add -u ".agent/rules/"
git add -u "docs/core/KUBEJS_CHEATSHEET.md"

:: 3. Commit
echo.
echo [2/3] Committing...
git commit -m "docs: consolidate project rules and kubejs manual, remove duplicates"

:: 4. Push Reminder
echo.
echo [3/3] DONE.
echo ==========================================
echo REMINDER: Run 'push_to_github.bat' now!
echo ==========================================
pause
