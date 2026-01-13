@echo off
echo ==========================================
echo      UPLOADING TO GITHUB (PUSH)
echo ==========================================

echo Current branch:
git branch --show-current
echo.

echo Pushing current branch to origin...
git push origin HEAD

echo.
echo ==========================================
echo DONE! Refresh your browser page now.
echo Note: If you don't see changes on 'main', 
echo switch to 'feat/core-mechanic' branch on GitHub.
echo ==========================================
pause
