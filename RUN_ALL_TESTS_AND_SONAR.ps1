# PowerShell script to run tests and push reports to SonarQube for all microservices

$services = @(
    "auth-service",
    "course-service",
    "discussion-service",
    "enrollment-service",
    "lesson-service",
    "notification-service",
    "progress-service"
)

Write-Host "Starting EduLearn Test and SonarQube Analysis..." -ForegroundColor Cyan

foreach ($service in $services) {
    Write-Host "`n======================================================================" -ForegroundColor Green
    Write-Host "Processing Service: $service" -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Green
    
    if (Test-Path $service) {
        Push-Location $service
        try {
            Write-Host "Running Maven tests and Sonar analysis for $service..." -ForegroundColor Yellow
            # Using -DskipTests=false to ensure tests run even if configured otherwise
            # Using verify to ensure jacoco:report runs
            mvn clean verify sonar:sonar -DskipTests=false
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Successfully completed $service" -ForegroundColor Green
            } else {
                Write-Host "Failed to complete $service" -ForegroundColor Red
            }
        } finally {
            Pop-Location
        }
    } else {
        Write-Host "Directory $service not found. Skipping." -ForegroundColor Red
    }
}

Write-Host "`nAll completed! Check your SonarQube dashboard at http://localhost:9000" -ForegroundColor Cyan
