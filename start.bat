@echo off
title PetBacked World App Starter
echo ===================================================
echo   PetBacked World Application Starting...
echo   Clearing invalid JAVA_HOME path to prevent crash
echo ===================================================
set JAVA_HOME=
call gradlew.bat bootRun --no-daemon
pause
