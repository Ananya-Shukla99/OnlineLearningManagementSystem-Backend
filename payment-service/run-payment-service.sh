#!/bin/bash

# ==================== Payment Service Startup Script ====================
# This script starts the Payment Service on port 8085 for Linux/Mac

echo ""
echo "========================================"
echo "  EDULEARN PAYMENT SERVICE STARTUP"
echo "  Port: 8085"
echo "========================================"
echo ""

# Check if Maven is installed
echo "[1/4] Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    echo "Please install Maven: brew install maven (Mac) or apt-get install maven (Linux)"
    exit 1
fi
echo "[✓] Maven is installed"

# Check if Java is installed
echo ""
echo "[2/4] Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    echo "Please install Java 17+: brew install openjdk@17 (Mac) or apt-get install openjdk-17-jdk (Linux)"
    exit 1
fi
echo "[✓] Java is installed"

# Clean and compile
echo ""
echo "[3/4] Cleaning and compiling payment-service..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "ERROR: Build compilation failed"
    exit 1
fi
echo "[✓] Build successful"

# Start the service
echo ""
echo "[4/4] Starting Payment Service on port 8085..."
echo ""
echo "========================================"
echo "  Service Status"
echo "========================================"
echo "  URL: http://localhost:8085/api/v1"
echo "  Swagger: http://localhost:8085/api/v1/swagger-ui.html"
echo "  Health: http://localhost:8085/api/v1/health"
echo ""
echo "  Press Ctrl+C to stop the service"
echo "========================================"
echo ""

mvn spring-boot:run

