@echo off
title WonManager - Korean Won Money Management App
echo ========================================================
echo   Launching WonManager (스마트 가계부)
echo ========================================================
java -jar "%~dp0build\compose\jars\MoneyManagementApp-windows-x64-1.0.0.jar"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%
    pause
)
