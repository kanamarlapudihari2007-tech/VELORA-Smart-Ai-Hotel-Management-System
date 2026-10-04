Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "[AI Smart Hotel] Build, Deploy & Restart Tomcat" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$env:CATALINA_HOME = "C:\apache-tomcat-9.0.122"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:JRE_HOME = "C:\Program Files\Java\jdk-17"

Write-Host "`n[1/4] Packaging WAR (fast, skipping tests)..." -ForegroundColor Yellow
mvn package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Maven build failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "`n[2/4] Stopping existing Tomcat..." -ForegroundColor Yellow
& "$env:CATALINA_HOME\bin\shutdown.bat" *>$null
Start-Sleep -Seconds 2
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

Write-Host "`n[3/4] Replacing WAR and purging cache..." -ForegroundColor Yellow
$explodedDir = "$env:CATALINA_HOME\webapps\ai-smart-hotel"
if (Test-Path $explodedDir) {
    Remove-Item $explodedDir -Recurse -Force -ErrorAction SilentlyContinue
}
Copy-Item "target\ai-smart-hotel.war" "$env:CATALINA_HOME\webapps\ai-smart-hotel.war" -Force

Write-Host "`n[4/4] Starting Tomcat in a separate window..." -ForegroundColor Yellow
Start-Process "cmd.exe" -ArgumentList "/k cd /d $env:CATALINA_HOME\bin && startup.bat"

Write-Host "`n===================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Rebuilt, Deployed and Tomcat Restarted!" -ForegroundColor Green
Write-Host "Waiting 5 seconds for application initialization..." -ForegroundColor Green
Start-Sleep -Seconds 5
Start-Process "http://localhost:8080/ai-smart-hotel/"
Write-Host "Web URL: http://localhost:8080/ai-smart-hotel/" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Green
