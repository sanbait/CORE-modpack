@echo off
call gradlew.bat build > build_log.txt 2>&1
echo Build exited with code %ERRORLEVEL% >> build_log.txt
