@echo off
echo Starting automated Lux-Core backup...

echo Check if git is installed
where git
if %errorlevel% neq 0 (
    echo [ERROR] Git is not installed or not in PATH.
    echo Please install Git from https://git-scm.com/downloads
    echo Close this window manually if needed.
    pause
    exit /b
)

echo Adding files...
git add .
set /p MSG="Enter commit message (Press Enter for 'Update'): "
if "%MSG%"=="" set MSG=Update
echo Committing with message: %MSG%
git commit -m "%MSG%"
echo Pushing to remote...
git push

echo.
echo Process finished.
echo If you see errors above, please read them.
pause
