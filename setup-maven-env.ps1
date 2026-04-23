# Setup Maven Environment for This Workspace
$env:M2_HOME = "D:\workspace-spring-tools-for-eclipse-5.0.1.RELEASE\OnlineLearningManagementSystem-Backend\apache-maven-3.8.1"
$env:Path = "$env:M2_HOME\bin;" + $env:Path
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"

Write-Host "Maven environment configured" -ForegroundColor Green
Write-Host "M2_HOME: $env:M2_HOME" -ForegroundColor Cyan
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
mvn --version | Select-Object -First 1

