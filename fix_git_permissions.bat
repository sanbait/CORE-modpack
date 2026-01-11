@echo off
echo Fixing Git 'Dubious Ownership' error...
git config --global --add safe.directory C:/Users/sanba/AppData/Roaming/PrismLauncher/instances/CORE
echo.
echo Permission fixed! Now try running 'save_progress.bat' again.
pause
