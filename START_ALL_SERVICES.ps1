# ============================================================================
# EduLearn Microservices Startup Script
# ============================================================================
# This script builds and starts all microservices in the correct order
# Services: auth-service -> course-service -> lesson-service -> enrollment-service -> assessment-service
# ============================================================================

#Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
#.\START_ALL_SERVICES.ps1
#PowerShell.exe -ExecutionPolicy Bypass -File .\START_ALL_SERVICES.ps1

param(
    [switch]$skipBuild = $false,
    [switch]$onlyAuth = $false
)

# Load environment variables from .env file
$envFilePath = Join-Path (Join-Path $PSScriptRoot "..") ".env"
if (Test-Path $envFilePath) {
    Write-Host "Loading environment variables from $envFilePath" -ForegroundColor Cyan
    Get-Content $envFilePath | Where-Object { $_ -match "=" } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
        Write-Host "  Set $name" -ForegroundColor Gray
    }
} else {
    Write-Host "No .env file found at $envFilePath" -ForegroundColor Yellow
}

# Check if RabbitMQ is running
Write-Host "Checking RabbitMQ Service..." -ForegroundColor Cyan
$rabbitService = Get-Service -Name "RabbitMQ" -ErrorAction SilentlyContinue
if ($rabbitService -eq $null) {
    Write-Host " RabbitMQ Service not found. Please ensure it is installed." -ForegroundColor Yellow
} elseif ($rabbitService.Status -ne 'Running') {
    Write-Host "Starting RabbitMQ Service..." -ForegroundColor Yellow
    Start-Service -Name "RabbitMQ" -ErrorAction SilentlyContinue
    if ($rabbitService.Status -ne 'Running') {
        Write-Host " Failed to start RabbitMQ automatically. Please start it manually." -ForegroundColor Red
    }
} else {
    Write-Host " RabbitMQ is running." -ForegroundColor Green
}

$BackendPath = "$PSScriptRoot"

$services = @(
    @{ name = "eureka-server"; port = 8761 },
    @{ name = "api-gateway"; port = 8080 },
    @{ name = "auth-service"; port = 8081 },
    @{ name = "course-service"; port = 8082 },
    @{ name = "lesson-service"; port = 8083 },
    @{ name = "enrollment-service"; port = 8084 },
    @{ name = "payment-service"; port = 8085 },
    @{ name = "notification-service"; port = 8087 },
    @{ name = "progress-service"; port = 8086 },
    @{ name = "discussion-service"; port = 8088 }
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

