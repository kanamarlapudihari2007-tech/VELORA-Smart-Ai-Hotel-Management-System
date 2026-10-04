@echo off
setlocal
title "AI Smart Hotel - Deploy & Restart"

set "CATALINA_HOME=C:\apache-tomcat-9.0.122"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "JRE_HOME=C:\Program Files\Java\jdk-17"

echo ===================================================
echo [AI Smart Hotel] Fast Build, Deploy ^& Tomcat Restart
echo ===================================================

echo.
echo [1/4] Packaging WAR (skipping tests for speed)...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/4] Stopping existing Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat" 1>nul 2>&1
ping 127.0.0.1 -n 3 >nul
taskkill /F /IM java.exe 1>nul 2>&1
ping 127.0.0.1 -n 2 >nul

echo.
echo [3/4] Clearing cache and deploying new WAR...
if exist "%CATALINA_HOME%\webapps\ai-smart-hotel" (
    rmdir /S /Q "%CATALINA_HOME%\webapps\ai-smart-hotel" 1>nul 2>&1
)
copy /Y "target\ai-smart-hotel.war" "%CATALINA_HOME%\webapps\ai-smart-hotel.war"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to copy WAR file! Check permissions.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [4/4] Launching Tomcat in a new window...
cd /d "%CATALINA_HOME%\bin"
start "Apache Tomcat 9" cmd.exe /k "startup.bat"
cd /d "%~dp0"

echo.
echo ===================================================
echo [SUCCESS] Rebuilt, Deployed and Tomcat is Starting!
echo Opening browser in 4 seconds...
echo Web URL: http://localhost:8080/ai-smart-hotel/
echo ===================================================
ping 127.0.0.1 -n 5 >nul
start http://localhost:8080/ai-smart-hotel/
