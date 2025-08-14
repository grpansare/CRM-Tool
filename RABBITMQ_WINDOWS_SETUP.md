# RabbitMQ Windows Setup Guide

## Prerequisites

Before installing RabbitMQ, you need to install **Erlang** first (RabbitMQ is built on Erlang).

### Step 1: Install Erlang

#### Option A: Using Chocolatey (Recommended)

```powershell
# Install Chocolatey first (if not already installed)
# Run PowerShell as Administrator and execute:
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Erlang
choco install erlang
```

#### Option B: Manual Installation

1. Go to: https://www.erlang.org/downloads
2. Download the latest Windows installer (e.g., `otp_win64_25.3.exe`)
3. Run the installer as Administrator
4. Follow the installation wizard
5. Add Erlang to PATH: `C:\Program Files\Erlang OTP\bin`

### Step 2: Install RabbitMQ

#### Option A: Using Chocolatey (Recommended)

```powershell
# Install RabbitMQ
choco install rabbitmq

# Start RabbitMQ service
Start-Service RabbitMQ
```

#### Option B: Manual Installation

1. Go to: https://www.rabbitmq.com/download.html
2. Download the Windows installer (e.g., `rabbitmq-server-3.12.0.exe`)
3. Run the installer as Administrator
4. Follow the installation wizard
5. The service should start automatically

### Step 3: Enable Management Plugin

```powershell
# Open Command Prompt as Administrator
# Navigate to RabbitMQ installation directory
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# Enable management plugin
rabbitmq-plugins enable rabbitmq_management

# Restart RabbitMQ service
Restart-Service RabbitMQ
```

### Step 4: Verify Installation

#### Check Service Status

```powershell
# Check if RabbitMQ service is running
Get-Service RabbitMQ

# Should show: Status: Running
```

#### Access Management UI

1. Open browser: http://localhost:15672
2. Login with default credentials:
   - **Username**: `guest`
   - **Password**: `guest`

#### Check Ports

```powershell
# Check if ports are listening
netstat -an | findstr :5672
netstat -an | findstr :15672

# Should show:
# TCP    0.0.0.0:5672    0.0.0.0:0    LISTENING
# TCP    0.0.0.0:15672   0.0.0.0:0    LISTENING
```

## Troubleshooting

### Common Issues

#### 1. Service Won't Start

```powershell
# Check service status
Get-Service RabbitMQ

# Check Windows Event Logs
Get-EventLog -LogName Application -Source RabbitMQ -Newest 10

# Manual start
Start-Service RabbitMQ
```

#### 2. Port Already in Use

```powershell
# Check what's using port 5672
netstat -ano | findstr :5672

# Kill the process if needed
taskkill /PID <PID> /F
```

#### 3. Management Plugin Not Working

```powershell
# Re-enable management plugin
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"
rabbitmq-plugins disable rabbitmq_management
rabbitmq-plugins enable rabbitmq_management
Restart-Service RabbitMQ
```

#### 4. Erlang Not Found

```powershell
# Check Erlang installation
erl -version

# If not found, add to PATH:
# C:\Program Files\Erlang OTP\bin
```

### Reset RabbitMQ (If Needed)

```powershell
# Stop service
Stop-Service RabbitMQ

# Remove data directory (WARNING: This deletes all data!)
Remove-Item "C:\Users\%USERNAME%\AppData\Roaming\RabbitMQ" -Recurse -Force

# Start service
Start-Service RabbitMQ

# Re-enable management plugin
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"
rabbitmq-plugins enable rabbitmq_management
```

## Configuration for CRM Platform

### Default Settings

Your CRM platform uses these default RabbitMQ settings:

- **Host**: localhost
- **Port**: 5672
- **Username**: guest
- **Password**: guest
- **Virtual Host**: /

### Queues Created Automatically

When your CRM services start, they will create:

- `contact-events`
- `account-events`
- `deal-events`

### Verify Queues in Management UI

1. Open: http://localhost:15672
2. Login: guest/guest
3. Go to "Queues" tab
4. You should see the queues after starting your services

## Quick Commands Reference

```powershell
# Service Management
Start-Service RabbitMQ
Stop-Service RabbitMQ
Restart-Service RabbitMQ
Get-Service RabbitMQ

# Plugin Management
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"
rabbitmq-plugins list
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins disable rabbitmq_management

# User Management
rabbitmqctl list_users
rabbitmqctl add_user admin admin123
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# Status Check
rabbitmqctl status
rabbitmqctl list_queues
rabbitmqctl list_exchanges
```

## Next Steps

1. **Verify RabbitMQ is running**: http://localhost:15672
2. **Start your CRM services** (they will create queues automatically)
3. **Check queues in Management UI** after services start
4. **Monitor message flow** in the Management UI

## Support

If you encounter issues:

1. Check Windows Event Logs
2. Verify Erlang installation
3. Ensure ports are not blocked by firewall
4. Run services as Administrator if needed

**Your RabbitMQ is now ready for the CRM platform!** 🐰
