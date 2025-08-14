# Start-Services.ps1
# Run this script from the root directory of the project

# Function to find Maven executable
function Get-MavenPath {
    # Common Maven installation paths
    $possiblePaths = @(
        "$env:MAVEN_HOME\bin\mvn.cmd",
        "$env:ProgramFiles\apache-maven\bin\mvn.cmd",
        "$env:ProgramFiles\Maven\bin\mvn.cmd",
        "C:\Program Files\apache-maven\bin\mvn.cmd",
        "C:\Program Files\Maven\bin\mvn.cmd"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            return $path
        }
    }
    
    throw "Maven not found. Please ensure Maven is installed and added to your PATH or set MAVEN_HOME environment variable."
}

# Get Maven path
$mvnPath = Get-MavenPath
Write-Host "Using Maven from: $mvnPath" -ForegroundColor Green

Write-Host "`nStarting CRM Platform Services..." -ForegroundColor Green

# 1. Start Eureka Server (Service Discovery)
Write-Host "`nStarting Eureka Server..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-eureka-server\pom.xml"
} catch {
    Write-Host "Error starting Eureka Server: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}

# Wait for Eureka to start
Write-Host "Waiting for Eureka Server to start (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 2. Start Config Server (if you have one)
# Start-Process -NoNewWindow -FilePath "mvn" -ArgumentList "spring-boot:run -f .\crm-config-server\pom.xml"
# Start-Sleep -Seconds 10

# 3. Start Email Service
Write-Host "`nStarting Email Service..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-email-service\pom.xml"
} catch {
    Write-Host "Error starting Email Service: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5

# 4. Start Auth Service
Write-Host "`nStarting Auth Service..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-auth-service\pom.xml"
} catch {
    Write-Host "Error starting Auth Service: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5

# 5. Start Contacts Service
Write-Host "`nStarting Contacts Service..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-contacts-service\pom.xml"
} catch {
    Write-Host "Error starting Contacts Service: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5

# 6. Start Sales Service
Write-Host "`nStarting Sales Service..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-sales-service\pom.xml"
} catch {
    Write-Host "Error starting Sales Service: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5

# 7. Start Activity Service
Write-Host "`nStarting Activity Service..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-activity-service\pom.xml"
} catch {
    Write-Host "Error starting Activity Service: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}

# Wait for all services to register with Eureka
Write-Host "`nWaiting for services to register with Eureka (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 8. Start API Gateway
Write-Host "`nStarting API Gateway..." -ForegroundColor Cyan
try {
    Start-Process -NoNewWindow -FilePath $mvnPath -ArgumentList "spring-boot:run -f .\crm-api-gateway\pom.xml"
} catch {
    Write-Host "Error starting API Gateway: $($Error[0].Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`nAll services started successfully!" -ForegroundColor Green
Write-Host "`nAccess the application at: http://localhost:3000" -ForegroundColor Green