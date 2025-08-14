# MongoDB Setup Guide for CRM Activity Service

This guide will help you set up MongoDB for the CRM Activity Service, which uses MongoDB to store high-volume activity data.

## 🚀 Quick Setup (Recommended)

### Option 1: MongoDB Community Server (Local Installation)

1. **Download MongoDB Community Server**
   - Visit: https://www.mongodb.com/try/download/community
   - Select your OS (Windows/macOS/Linux)
   - Download and install

2. **Start MongoDB Service**
   
   **Windows:**
   ```cmd
   # Start MongoDB as a service (if installed as service)
   net start MongoDB
   
   # Or start manually
   "C:\Program Files\MongoDB\Server\7.0\bin\mongod.exe" --dbpath "C:\data\db"
   ```
   
   **macOS:**
   ```bash
   # Using Homebrew
   brew services start mongodb-community
   
   # Or manually
   mongod --config /usr/local/etc/mongod.conf
   ```
   
   **Linux:**
   ```bash
   sudo systemctl start mongod
   sudo systemctl enable mongod
   ```

3. **Verify Installation**
   ```bash
   # Connect to MongoDB shell
   mongosh
   
   # Should show MongoDB connection info
   ```

### Option 2: MongoDB Atlas (Cloud - Free Tier Available)

1. **Create Account**
   - Visit: https://www.mongodb.com/cloud/atlas
   - Sign up for free account

2. **Create Cluster**
   - Choose M0 Sandbox (Free)
   - Select region closest to you
   - Create cluster

3. **Configure Access**
   - Add your IP address to whitelist
   - Create database user with read/write permissions

4. **Get Connection String**
   - Click "Connect" → "Connect your application"
   - Copy connection string
   - Update `application.properties` with your connection string

### Option 3: Docker (Quick Development Setup)

```bash
# Run MongoDB in Docker
docker run -d \
  --name crm-mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:7.0

# Verify it's running
docker ps
```

## 🔧 Configuration

### Default Configuration (Local MongoDB)
The Activity Service is pre-configured for local MongoDB:

```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=crm_activities
spring.data.mongodb.authentication-database=admin
```

### Custom Configuration
If you need to customize the connection, update `crm-activity-service/src/main/resources/application.properties`:

```properties
# For MongoDB with authentication
spring.data.mongodb.host=your-host
spring.data.mongodb.port=27017
spring.data.mongodb.database=crm_activities
spring.data.mongodb.username=your-username
spring.data.mongodb.password=your-password
spring.data.mongodb.authentication-database=admin

# For MongoDB Atlas (Cloud)
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/crm_activities?retryWrites=true&w=majority
```

## 📊 Database Schema

The Activity Service will automatically create the following structure:

### Collection: `activities`
```javascript
{
  "_id": ObjectId("..."),
  "tenant_id": 101,
  "activity_id": "act_12345678",
  "user_id": 205,
  "type": "CALL",
  "timestamp": "2025-07-26T14:30:00Z",
  "content": "Called John Smith about the proposal...",
  "outcome": "Connected",
  "associations": {
    "contacts": [312, 315],
    "accounts": [58],
    "deals": [92]
  }
}
```

### Indexes (Auto-created)
- `tenant_id` - For multi-tenant data isolation
- `tenant_id + associations.contacts` - For contact timelines
- `tenant_id + associations.accounts` - For account timelines
- `tenant_id + associations.deals` - For deal timelines
- `tenant_id + user_id` - For user activity queries
- `timestamp` - For chronological sorting

## 🧪 Testing Connection

1. **Start MongoDB** (using any method above)

2. **Start the Activity Service**
   ```bash
   cd crm-activity-service
   mvn spring-boot:run
   ```

3. **Check Logs**
   Look for successful MongoDB connection:
   ```
   INFO  o.s.d.m.c.MongoTemplate - Connected to MongoDB at localhost:27017
   INFO  c.c.activity.ActivityServiceApplication - Started ActivityServiceApplication
   ```

4. **Test API Endpoint**
   ```bash
   # Health check
   curl http://localhost:8084/actuator/health
   
   # Should return: {"status":"UP"}
   ```

## 🔍 Monitoring & Management

### MongoDB Compass (GUI Tool)
- Download: https://www.mongodb.com/products/compass
- Connect to: `mongodb://localhost:27017`
- Browse collections and documents visually

### Command Line Tools
```bash
# Connect to MongoDB shell
mongosh

# Switch to activities database
use crm_activities

# View collections
show collections

# Query activities
db.activities.find().limit(5)

# Count documents
db.activities.countDocuments()
```

## 🚨 Troubleshooting

### Common Issues

**Connection Refused:**
- Ensure MongoDB is running
- Check port 27017 is not blocked
- Verify connection string

**Authentication Failed:**
- Check username/password
- Verify authentication database
- Ensure user has proper permissions

**Database Not Found:**
- MongoDB creates databases automatically
- Ensure service has write permissions

**Performance Issues:**
- Check if indexes are created
- Monitor memory usage
- Consider sharding for large datasets

### Logs to Check
```bash
# Activity Service logs
tail -f crm-activity-service/logs/application.log

# MongoDB logs (Linux/macOS)
tail -f /var/log/mongodb/mongod.log

# Windows MongoDB logs
# Check Windows Event Viewer or MongoDB log directory
```

## 📈 Production Considerations

### Security
- Enable authentication
- Use SSL/TLS connections
- Implement proper user roles
- Regular security updates

### Performance
- Configure appropriate indexes
- Monitor query performance
- Set up replica sets for high availability
- Consider sharding for large datasets

### Backup
- Set up automated backups
- Test restore procedures
- Consider point-in-time recovery

---

## 🎯 Quick Start Summary

1. **Install MongoDB** (Community Server recommended)
2. **Start MongoDB service**
3. **Verify connection** with `mongosh`
4. **Start Activity Service** with `mvn spring-boot:run`
5. **Test** with health check endpoint

The Activity Service will automatically create the database and collections when it starts!
