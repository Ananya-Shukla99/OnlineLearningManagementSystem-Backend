# ============================================================================
# EduLearn Microservices Startup Script
# ============================================================================
# This script builds and starts all microservices in the correct order
# Services: auth-service -> course-service -> lesson-service -> enrollment-service -> assessment-service
# ============================================================================

param(
    [switch]$skipBuild = $false,
    [switch]$onlyAuth = $false
)

$BackendPath = "D:\workspace-spring-tools-for-eclipse-5.0.1.RELEASE\OnlineLearningManagementSystem-Backend"

# Define services in order
$services = @(
    @{ name = "auth-service"; port = 8081 },
    @{ name = "course-service"; port = 8082 },
    @{ name = "lesson-service"; port = 8083 },
    @{ name = "enrollment-service"; port = 8084 },
    @{ name = "assessment-service"; port = 8085 }
)

# Color functions
function Write-Success { Write-Host $args[0] -ForegroundColor Green }
function Write-Error-Message { Write-Host $args[0] -ForegroundColor Red }
function Write-Info { Write-Host $args[0] -ForegroundColor Cyan }
function Write-Warning-Message { Write-Host $args[0] -ForegroundColor Yellow }

Write-Info "==============================================="
Write-Info "EduLearn Microservices Startup"
Write-Info "==============================================="

# Step 1: Build all services if not skipped
if (-not $skipBuild) {
    Write-Info "`nStep 1: Building all services..."

    foreach ($service in $services) {
        $servicePath = Join-Path $BackendPath $service.name
        Write-Info "Building $($service.name)..."

        Set-Location $servicePath
        mvn clean install -DskipTests

        if ($LASTEXITCODE -ne 0) {
            Write-Error-Message "Failed to build $($service.name)"
            exit 1
        }
        Write-Success "$($service.name) built successfully"
    }
}

# Step 2: Start services
Write-Info "`nStep 2: Starting services..."
Write-Warning-Message "IMPORTANT: Make sure MySQL is running on localhost:3306"
Write-Warning-Message "Database credentials: root / root123"

$processes = @()

foreach ($service in $services) {
    if ($onlyAuth -and $service.name -ne "auth-service") {
        continue
    }

    $servicePath = Join-Path $BackendPath $service.name
    Write-Info "`nStarting $($service.name) on port $($service.port)..."

    Set-Location $servicePath

    # Start service in a new PowerShell window
    $processName = "mvn spring-boot:run"
    $windowTitle = "$($service.name) - Port $($service.port)"

    $process = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$servicePath'; Write-Host 'Starting $($service.name)...'; mvn spring-boot:run" -PassThru

    $processes += $process
    Write-Success "$($service.name) started (PID: $($process.Id))"

    # Wait a few seconds between service starts
    Start-Sleep -Seconds 3
}

Write-Success "`n==============================================="
Write-Success "All services started successfully!"
Write-Success "==============================================="
Write-Info "Service Endpoints:"
foreach ($service in $services) {
    Write-Info "  $($service.name): http://localhost:$($service.port)/api/v1/swagger-ui.html"
}

Write-Info "`nRunning processes:"
foreach ($process in $processes) {
    Write-Info "  PID $($process.Id): $($process.ProcessName)"
}

Write-Warning-Message "`nTo stop all services, close the PowerShell windows or use Task Manager"

