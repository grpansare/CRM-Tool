# 🏢 CRM Platform - Multi-Tenant SaaS Solution

A comprehensive, enterprise-grade CRM platform built with Spring Boot microservices architecture, featuring multi-tenancy, role-based access control, and modern SaaS capabilities.

## 🎯 **Standard Tenant Registration Approach**

### **Overview**

The CRM platform implements a **standard SaaS tenant registration flow** that follows industry best practices for B2B software onboarding.

### **Registration Flow**

#### **1. Tenant Self-Registration**

```http
POST /api/v1/tenants/register
```

**Request Body:**

```json
{
  "tenantName": "Acme Corporation",
  "subdomain": "acme",
  "companyName": "Acme Corporation",
  "adminFirstName": "John",
  "adminLastName": "Doe",
  "adminEmail": "john.doe@acme.com",
  "adminUsername": "johndoe",
  "adminPassword": "SecurePass123!",
  "companyAddress": "123 Business St, City, State",
  "companyPhone": "+1-555-0123",
  "companyEmail": "info@acme.com",
  "industry": "Technology",
  "timezone": "America/New_York",
  "locale": "en-US",
  "subscriptionPlan": "STARTER",
  "acceptTerms": true,
  "acceptMarketing": false
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "tenantId": 2,
    "tenantName": "Acme Corporation",
    "subdomain": "acme",
    "companyName": "Acme Corporation",
    "subscriptionPlan": "STARTER",
    "trialEndsAt": "2024-02-15T10:30:00Z",
    "createdAt": "2024-02-01T10:30:00Z",
    "adminUserId": 2,
    "adminEmail": "john.doe@acme.com",
    "adminUsername": "johndoe",
    "welcomeMessage": "Welcome to Acme Corporation!",
    "nextSteps": "Please check your email to verify your account and get started."
  }
}
```

#### **2. Subdomain Availability Check**

```http
GET /api/v1/tenants/check-subdomain/{subdomain}
```

#### **3. Subscription Plans**

```http
GET /api/v1/tenants/subscription-plans
```

### **User Registration Process**

#### **For New Organizations:**

1. **Tenant Registration**: Organization registers via `/api/v1/tenants/register`
2. **Admin Creation**: System automatically creates tenant admin user
3. **Welcome Email**: Admin receives welcome email with login credentials
4. **Account Verification**: Admin verifies email and completes setup

#### **For New Users in Existing Organization:**

1. **Admin Invitation**: Tenant admin invites new users via `/api/v1/users`
2. **User Creation**: System creates user with appropriate role and permissions
3. **Invitation Email**: User receives invitation email with login credentials
4. **User Onboarding**: User completes profile setup and starts using the system

### **Role Hierarchy**

```
SUPER_ADMIN (System Level)
├── TENANT_ADMIN (Organization Level)
    ├── SALES_MANAGER (Team Level)
    │   ├── SALES_REP (Individual Level)
    │   └── SALES_REP
    ├── SUPPORT_AGENT
    └── READ_ONLY
```

### **Subscription Plans**

| Plan             | Users | Price      | Features                                                                |
| ---------------- | ----- | ---------- | ----------------------------------------------------------------------- |
| **FREE**         | 5     | $0/month   | Basic CRM, Email Support                                                |
| **STARTER**      | 25    | $29/month  | Full CRM, Priority Support, Basic Reports                               |
| **PROFESSIONAL** | 100   | $99/month  | Advanced CRM, 24/7 Support, Advanced Reports, API Access                |
| **ENTERPRISE**   | 1000  | $299/month | Enterprise CRM, Dedicated Support, Custom Reports, Full API Access, SSO |

### **Security Features**

- **Multi-Tenant Data Isolation**: Complete data separation between tenants
- **Role-Based Access Control**: Granular permissions based on user roles
- **Hierarchy-Based Access**: Managers can access team member data
- **JWT Authentication**: Secure token-based authentication
- **Password Policies**: Strong password requirements
- **Email Verification**: Required for all new accounts

### **API Endpoints**

#### **Public Endpoints (No Authentication Required)**

```http
POST   /api/v1/tenants/register          # Tenant registration
GET    /api/v1/tenants/check-subdomain/{subdomain}  # Check availability
GET    /api/v1/tenants/subscription-plans # Get plans
POST   /api/v1/auth/login                # User login
```

#### **Protected Endpoints (Authentication Required)**

```http
# Tenant Management
GET    /api/v1/tenants/{tenantId}        # Get tenant info
PUT    /api/v1/tenants/{tenantId}        # Update tenant
GET    /api/v1/tenants/by-subdomain/{subdomain}  # Get by subdomain

# User Management
POST   /api/v1/users                     # Create user
GET    /api/v1/users                     # List users
GET    /api/v1/users/team                # Get team members
GET    /api/v1/users/{id}                # Get user

# Authentication
POST   /api/v1/auth/validate             # Validate token
GET    /api/v1/auth/me                   # Current user info
```

### **Implementation Benefits**

1. **Self-Service Onboarding**: Organizations can register without manual intervention
2. **Scalable Architecture**: Supports thousands of tenants
3. **Flexible Pricing**: Multiple subscription tiers
4. **Security First**: Enterprise-grade security features
5. **User-Friendly**: Intuitive registration and onboarding process
6. **Compliance Ready**: Supports GDPR, SOC2, and other compliance requirements

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Mobile App    │    │   Third Party   │
│   (React/Vue)   │    │   (React Native)│    │   Integrations  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │     API Gateway           │
                    │   (Port: 8080)            │
                    │  - Authentication         │
                    │  - Routing                │
                    │  - Circuit Breaker        │
                    │  - Load Balancing         │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │   Eureka Server           │
                    │   (Port: 8761)            │
                    │  - Service Discovery      │
                    └─────────────┬─────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
┌─────────▼─────────┐  ┌─────────▼─────────┐  ┌─────────▼─────────┐
│   Auth Service     │  │ Contacts Service  │  │   Sales Service   │
│   (Port: 8083)     │  │   (Port: 8081)    │  │   (Port: 8082)    │
│  - Authentication  │  │  - Contacts Mgmt  │  │  - Deals Mgmt     │
│  - Authorization   │  │  - Accounts Mgmt  │  │  - Pipeline Mgmt  │
│  - User Mgmt       │  │  - Duplicate Det. │  │  - Forecasting    │
└────────────────────┘  └────────────────────┘  └────────────────────┘
```

## 🚀 Current Status

### ✅ **Completed Services**

1. **🔐 Auth Service** - Complete

   - JWT Authentication & Authorization
   - Multi-tenant user management
   - Role-based access control (RBAC)
   - Hierarchy-based permissions
   - User registration & login

2. **👥 Contacts Service** - Complete

   - Contact & Account management
   - Duplicate detection
   - Custom fields support
   - Account-Contact relationships

3. **💰 Sales Service** - Complete

   - Deal management
   - Sales pipeline
   - Stage tracking
   - Integration with Contacts service

4. **🌐 API Gateway** - Complete

   - Request routing
   - Authentication filter
   - Circuit breaker protection
   - Load balancing
   - CORS handling

5. **🔍 Eureka Server** - Complete
   - Service discovery
   - Health monitoring
   - Service registration

### 🚧 **In Progress**

- **📊 Activity Service** - Next to implement
- **🧪 Testing** - Unit & integration tests
- **🐳 Containerization** - Docker setup

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Cloud**: Spring Cloud 2023.0.0
- **Database**: MySQL
- **Authentication**: JWT (JSON Web Tokens)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaker**: Resilience4j
- **Build Tool**: Maven
- **Language**: Java 17

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

## 🚀 Quick Start

### 1. **Clone the Repository**

```bash
git clone <repository-url>
cd crm-platform
```

### 2. **Database Setup**

```sql
-- Create databases
CREATE DATABASE crm_auth;
CREATE DATABASE crm_contacts;
CREATE DATABASE crm_sales;

-- Create user
CREATE USER crm_user WITH PASSWORD 'crm_password';
GRANT ALL PRIVILEGES ON DATABASE crm_auth TO crm_user;
GRANT ALL PRIVILEGES ON DATABASE crm_contacts TO crm_user;
GRANT ALL PRIVILEGES ON DATABASE crm_sales TO crm_user;
```

### 3. **Run Database Migrations**

```bash
# Run the SQL scripts in each service's db/migration folder
# - crm-auth-service/src/main/resources/db/migration/V1__Create_auth_tables.sql
# - crm-contacts-service/src/main/resources/db/migration/V1__Create_contacts_tables.sql
# - crm-sales-service/src/main/resources/db/migration/V1__Create_sales_tables.sql
```

### 4. **Start Services (in order)**

```bash
# 1. Start Eureka Server (Service Discovery)
cd crm-eureka-server
mvn spring-boot:run

# 2. Start Auth Service
cd ../crm-auth-service
mvn spring-boot:run

# 3. Start Contacts Service
cd ../crm-contacts-service
mvn spring-boot:run

# 4. Start Sales Service
cd ../crm-sales-service
mvn spring-boot:run

# 5. Start API Gateway
cd ../crm-api-gateway
mvn spring-boot:run
```

### 5. **Verify Services**

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8083
- **Contacts Service**: http://localhost:8081
- **Sales Service**: http://localhost:8082

## 🔐 Authentication

### **Default Admin User**

```json
{
  "email": "admin@crmplatform.com",
  "password": "admin123",
  "role": "SUPER_ADMIN"
}
```

### **Login Flow**

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@crmplatform.com",
  "password": "admin123"
}
```

### **Response**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "userId": 1,
      "tenantId": 1,
      "username": "admin",
      "role": "SUPER_ADMIN"
    }
  }
}
```

## 📚 API Documentation

### **Authentication Endpoints**

```http
POST /api/v1/auth/login          # User login
POST /api/v1/auth/validate       # Token validation
GET  /api/v1/auth/me             # Current user info
```

### **Contacts Endpoints**

```http
POST   /api/v1/contacts          # Create contact
GET    /api/v1/contacts          # List contacts
GET    /api/v1/contacts/{id}     # Get contact
POST   /api/v1/accounts          # Create account
GET    /api/v1/accounts          # List accounts
GET    /api/v1/accounts/{id}     # Get account
```

### **Sales Endpoints**

```http
POST   /api/v1/deals             # Create deal
GET    /api/v1/deals             # List deals
GET    /api/v1/deals/{id}        # Get deal
PUT    /api/v1/deals/{id}/stage  # Update deal stage
```

### **User Management Endpoints**

```http
POST   /api/v1/users             # Create user
GET    /api/v1/users             # List users
GET    /api/v1/users/team        # Get team members
GET    /api/v1/users/{id}        # Get user
```

## 🔒 Security Features

### **Multi-Tenancy**

- Complete data isolation between tenants
- Tenant-specific user management
- Automatic tenant filtering in all queries

### **Role-Based Access Control**

- **SUPER_ADMIN**: Full system access
- **TENANT_ADMIN**: Tenant-level administration
- **SALES_MANAGER**: Team management & reporting
- **SALES_REP**: Own data access only
- **SUPPORT_AGENT**: Read access with limited write
- **READ_ONLY**: View-only access

### **Hierarchy-Based Access**

- Sales managers can see team members' data
- Sales reps can only see their own data
- Manager-employee relationships tracked

## 🎯 Next Steps

### **Priority 1: Activity Service**

```java
// Implement Activity Service for:
- Activity logging (calls, emails, meetings, notes)
- Timeline generation
- Activity tracking per contact/deal
- Integration with other services
```

### **Priority 2: Security Integration**

```java
// Add to each microservice:
- JWT token validation
- User context extraction from headers
- Multi-tenancy enforcement
- Role-based access control
```

### **Priority 3: Testing**

```java
// Comprehensive testing:
- Unit tests for each service
- Integration tests
- End-to-end API testing
- Performance testing
```

### **Priority 4: Containerization**

```dockerfile
# Docker containers for each service
- Dockerfile for each microservice
- Docker Compose for local development
- Kubernetes manifests for production
```

### **Priority 5: Monitoring & Observability**

```yaml
# Add monitoring:
- Prometheus metrics
- Grafana dashboards
- Distributed tracing (Jaeger)
- Centralized logging (ELK Stack)
```

## 🧪 Testing

### **Unit Tests**

```bash
# Run unit tests for all services
mvn test
```

### **Integration Tests**

```bash
# Run integration tests
mvn verify
```

### **API Testing**

```bash
# Test API endpoints
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@crmplatform.com","password":"admin123"}'
```

## 🐳 Docker Support

### **Build Images**

```bash
# Build all services
mvn clean package -DskipTests

# Build Docker images
docker build -t crm-eureka-server ./crm-eureka-server
docker build -t crm-auth-service ./crm-auth-service
docker build -t crm-contacts-service ./crm-contacts-service
docker build -t crm-sales-service ./crm-sales-service
docker build -t crm-api-gateway ./crm-api-gateway
```

### **Docker Compose**

```bash
# Start all services with Docker Compose
docker-compose up -d
```

## 📊 Monitoring

### **Health Checks**

```http
GET /actuator/health          # Service health
GET /actuator/info           # Application info
GET /actuator/metrics        # Performance metrics
```

### **Eureka Dashboard**

- **URL**: http://localhost:8761
- **Features**: Service discovery, health monitoring

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the documentation

---

**🏢 CRM Platform** - Built with ❤️ using Spring Boot Microservices
