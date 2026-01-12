@echo off
echo Committing Optimize Fixes and Config Integration...
git add .
git commit -m "Fix: FPS Drop (DistExecutor removal), KubeJS Items, Particle Opt"
echo.
echo Commit successful! Now run push_to_github.bat
pause
