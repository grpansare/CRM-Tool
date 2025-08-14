# MySQL Setup Guide for CRM Platform

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- RabbitMQ 3.8+ (DISABLED - no longer required)
- Git

## Database Setup

### 1. Create MySQL Databases

Connect to MySQL and create the required databases:

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create databases
CREATE DATABASE crm_auth;
CREATE DATABASE crm_contacts;
CREATE DATABASE crm_sales;

-- Create user and grant permissions
CREATE USER 'crm_user'@'localhost' IDENTIFIED BY 'crm_password';
GRANT ALL PRIVILEGES ON crm_auth.* TO 'crm_user'@'localhost';
GRANT ALL PRIVILEGES ON crm_contacts.* TO 'crm_user'@'localhost';
GRANT ALL PRIVILEGES ON crm_sales.* TO 'crm_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify databases
SHOW DATABASES;
```

## RabbitMQ Setup (DISABLED)

**Note**: RabbitMQ has been disabled in the current configuration. Event publishing is no longer required for the CRM platform to function. You can skip this section entirely.

### 1. Install RabbitMQ

#### **Windows:**

```powershell
# Using Chocolatey (recommended)
choco install rabbitmq

# Or download from https://www.rabbitmq.com/download.html
# Install Erlang first, then RabbitMQ
```

#### **macOS:**

```bash
# Using Homebrew
brew install rabbitmq

# Start RabbitMQ service
brew services start rabbitmq
```

#### **Linux (Ubuntu/Debian):**

```bash
# Install Erlang
sudo apt-get install erlang

# Install RabbitMQ
sudo apt-get install rabbitmq-server

# Start RabbitMQ service
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server
```

### 2. Enable Management Plugin

```bash
# Enable the management plugin
rabbitmq-plugins enable rabbitmq_management

# Restart RabbitMQ service
# Windows: Restart from Services
# macOS: brew services restart rabbitmq
# Linux: sudo systemctl restart rabbitmq-server
```

### 3. Create Admin User

```bash
# Create admin user (optional, guest/guest is default)
rabbitmqctl add_user admin admin123
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

### 4. Verify Installation

- **Management UI**: http://localhost:15672
- **Default Credentials**: guest/guest
- **Port**: 5672 (AMQP), 15672 (Management)

### 2. Database Configuration

All services are now configured to use MySQL:

- **Auth Service**: `crm_auth` database
- **Contacts Service**: `crm_contacts` database
- **Sales Service**: `crm_sales` database

## Running Services

### Prerequisites Check

Before starting services, ensure:

1. **MySQL** is running and databases are created
2. **RabbitMQ** is no longer required (event publishing disabled)
3. **Java 17+** is installed and configured

### Start Order (Important!)

1. **Eureka Server** (Service Discovery)
2. **Auth Service** (Authentication)
3. **Contacts Service** (Contact Management)
4. **Sales Service** (Sales Management)
5. **API Gateway** (Routing)
6. **Frontend** (User Interface)

### PowerShell Commands

Open **6 separate PowerShell terminals**:

#### Terminal 1: Eureka Server

```powershell
cd "crm-eureka-server"
mvn spring-boot:run
```

#### Terminal 2: Auth Service

```powershell
cd "crm-auth-service"
mvn spring-boot:run
```

#### Terminal 3: Contacts Service

```powershell
cd "crm-contacts-service"
mvn spring-boot:run
```

#### Terminal 4: Sales Service

```powershell
cd "crm-sales-service"
mvn spring-boot:run
```

#### Terminal 5: API Gateway

```powershell
cd "crm-api-gateway"
mvn spring-boot:run
```

#### Terminal 6: Frontend

```powershell
cd "frontend"
npm run dev
```

### Alternative: Command Prompt

If PowerShell gives issues, use Command Prompt:

```cmd
# Terminal 1
cd crm-eureka-server
mvn spring-boot:run

# Terminal 2
cd crm-auth-service
mvn spring-boot:run

# Terminal 3
cd crm-contacts-service
mvn spring-boot:run

# Terminal 4
cd crm-sales-service
mvn spring-boot:run

# Terminal 5
cd crm-api-gateway
mvn spring-boot:run

# Terminal 6
cd frontend
npm run dev
```

## Access URLs

Once all services are running:

```
Frontend: http://localhost:3000
API Gateway: http://localhost:8080
Auth Service: http://localhost:8083
Contacts Service: http://localhost:8081
Sales Service: http://localhost:8082
Eureka Server: http://localhost:8761
```

## Verification

### 1. Check Infrastructure Services

- **MySQL**: Ensure databases are accessible
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Eureka Dashboard**: http://localhost:8761

### 2. Check Eureka Dashboard

- Open http://localhost:8761
- You should see all services registered

### 2. Check Database Tables

```sql
-- Check Auth Service tables
USE crm_auth;
SHOW TABLES;

-- Check Contacts Service tables
USE crm_contacts;
SHOW TABLES;

-- Check Sales Service tables
USE crm_sales;
SHOW TABLES;
```

### 3. Check RabbitMQ Queues (Optional)

- Open http://localhost:15672
- Login with guest/guest
- Check that queues are created automatically:
  - `contact-events`
  - `account-events`
  - `deal-events`

**Note**: RabbitMQ is optional. If not installed, event publishing will be skipped but core functionality will work.

### 4. Test Frontend

- Open http://localhost:3000
- You should see the CRM platform

## Troubleshooting

### Port Conflicts

```powershell
# Check what's using a port
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F
```

### Database Connection Issues

1. Ensure MySQL is running
2. Check database exists
3. Verify user permissions
4. Check application.properties

### RabbitMQ Connection Issues (Optional)

1. Ensure RabbitMQ is running
2. Check port 5672 is accessible
3. Verify management plugin is enabled
4. Check application.properties for stream bindings

**Note**: If RabbitMQ is not available, the application will start without event publishing functionality.

### Service Not Starting

1. Check logs for errors
2. Verify dependencies are installed
3. Check Java version (requires Java 17+)

## Database Schema

### Auth Service Tables

- `users` - User accounts
- `tenants` - Organization/tenant information
- `user_permissions` - User permissions

### Contacts Service Tables

- `accounts` - Company accounts
- `contacts` - Contact information
- `account_contacts` - Account-contact relationships
- `custom_fields_data` - Custom field data

### Sales Service Tables

- `sales_pipelines` - Sales pipelines
- `pipeline_stages` - Pipeline stages
- `deals` - Sales deals
- `deal_stage_history` - Deal stage changes

## Quick Start Script

Create `start-all.bat` in project root:

```batch
@echo off
echo Starting CRM Platform with MySQL and RabbitMQ...

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

echo Starting API Gateway...
start "API Gateway" cmd /k "cd crm-api-gateway && mvn spring-boot:run"
timeout /t 5

echo Starting Frontend...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo All services started!
echo Frontend: http://localhost:3000
echo Eureka: http://localhost:8761
pause
```

## Multi-Tenant Mode

The system is configured for multi-tenant mode:

1. **Organization Registration** - Each organization gets a unique subdomain
2. **Tenant Isolation** - Data is separated by tenant_id
3. **Subdomain Routing** - Organizations access via their subdomain
4. **Demo Ready** - Perfect for internship presentation with multiple organizations

## Next Steps

1. Start all services
2. Access http://localhost:3000
3. Register a user account
4. Explore the CRM features
5. Demo the functionality

**Your CRM platform is now ready to run with MySQL!** 🚀
