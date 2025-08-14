@echo off
echo Starting CRM Platform with MySQL (Multi-Tenant Mode)...

echo Starting Eureka Server...
start "Eureka Server" cmd /k "cd crm-eureka-server && mvn spring-boot:run"
timeout /t 10

echo Starting Auth Service...
start "Auth Service" cmd /k "cd crm-auth-service && mvn spring-boot:run"
timeout /t 5

echo Starting Contacts Service...
start "Contacts Service" cmd /k "cd crm-contacts-service && mvn spring-boot:run"
timeout /t 5

echo Starting Sales Service...
start "Sales Service" cmd /k "cd crm-sales-service && mvn spring-boot:run"
timeout /t 5

echo Starting Activity Service...
start "Activity Service" cmd /k "cd crm-activity-service && mvn spring-boot:run"
timeout /t 5

echo Starting Email Service...
start "Email Service" cmd /k "cd crm-email-service && mvn spring-boot:run"
timeout /t 5

echo Starting API Gateway...
start "API Gateway" cmd /k "cd crm-api-gateway && mvn spring-boot:run"
timeout /t 5

echo Starting Frontend...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo All services started!
echo.
echo Access URLs:
echo Frontend: http://localhost:3000
echo Eureka Dashboard: http://localhost:8761
echo API Gateway: http://localhost:8080
echo.
echo Multi-Tenant Demo:
echo - Register organizations at: http://localhost:3000/register
echo - Each org gets a unique subdomain
echo - Access via: http://localhost:3000?tenant=YOUR_SUBDOMAIN
echo.
pause 