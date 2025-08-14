# Demo Setup Guide for CRM Platform

## How to Run Without Real Domains

### For Your Internship Demo - You DON'T Need Real Domains!

## 1. Local Development Setup

### Start All Services

```bash
# Terminal 1: Eureka Server
cd crm-eureka-server
mvn spring-boot:run

# Terminal 2: API Gateway
cd crm-api-gateway
mvn spring-boot:run

# Terminal 3: Auth Service
cd crm-auth-service
mvn spring-boot:run

# Terminal 4: Contacts Service
cd crm-contacts-service
mvn spring-boot:run

# Terminal 5: Sales Service
cd crm-sales-service
mvn spring-boot:run

# Terminal 6: Frontend
cd frontend
npm run dev
```

### Access URLs

```
Frontend: http://localhost:3000
API Gateway: http://localhost:8080
Auth Service: http://localhost:8083
Contacts Service: http://localhost:8081
Sales Service: http://localhost:8082
Eureka Server: http://localhost:8761
```

## 2. Subdomain Simulation

### How It Works

Instead of real subdomains like `acme.crmplatform.com`, we simulate them using URL parameters:

```
http://localhost:3000/dashboard?tenant=acme
http://localhost:3000/dashboard?tenant=techcorp
http://localhost:3000/dashboard?tenant=salescompany
```

### Demo Scenarios

#### Scenario 1: Organization Registration

1. Go to `http://localhost:3000/register`
2. Fill organization details
3. Enter subdomain: "acme"
4. Complete registration
5. Access: `http://localhost:3000/dashboard?tenant=acme`

#### Scenario 2: Multiple Tenants

1. Register "Acme Corp" with subdomain "acme"
2. Register "TechCorp" with subdomain "techcorp"
3. Switch between tenants:
   - `http://localhost:3000/dashboard?tenant=acme`
   - `http://localhost:3000/dashboard?tenant=techcorp`

#### Scenario 3: Subdomain Validation

1. Try to register with existing subdomain
2. See real-time validation
3. Try reserved words like "admin", "api"
4. See validation errors

## 3. What to Explain in Your Demo

### Technical Implementation

```javascript
// Frontend: Subdomain simulation
const getCurrentSubdomain = () => {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('tenant') || 'demo';
};

// Backend: Real subdomain validation
public boolean isSubdomainAvailable(String subdomain) {
  // Format validation
  // Reserved word check
  // Database uniqueness check
  return !tenantRepository.existsBySubdomain(subdomain);
}
```

### Multi-tenancy Architecture

- **Database Isolation**: Each tenant has isolated data
- **Service Discovery**: Eureka manages service instances
- **API Gateway**: Routes requests to appropriate services
- **Authentication**: JWT tokens with tenant context

### Security Features

- Subdomain validation and sanitization
- Reserved word protection
- SQL injection prevention
- Tenant data isolation

## 4. Demo Script

### Introduction (2 minutes)

"Today I'll demonstrate a multi-tenant CRM platform with custom subdomain support. This is a full-stack application built with Spring Boot microservices and React frontend."

### Registration Flow (3 minutes)

1. "Let me show you the organization registration process"
2. Navigate to registration page
3. Fill form with organization details
4. "Notice the real-time subdomain availability checking"
5. Complete registration
6. "The system creates a new tenant with isolated data"

### Multi-tenancy Demo (3 minutes)

1. "Now let me show you how multiple organizations can coexist"
2. Register another organization
3. "Each tenant gets their own workspace"
4. Switch between tenants using URL parameters
5. "Notice how the data is completely isolated"

### Technical Deep Dive (2 minutes)

1. "Let me show you the backend implementation"
2. Open Eureka dashboard
3. Show service discovery
4. "The system uses microservices architecture"
5. "Each service handles specific business logic"

### Q&A (2 minutes)

"Any questions about the implementation or architecture?"

## 5. Key Points to Emphasize

### Architecture

- ✅ Microservices with Spring Boot
- ✅ Service Discovery with Eureka
- ✅ API Gateway for routing
- ✅ Multi-tenant database design
- ✅ JWT authentication

### Features

- ✅ Real-time subdomain validation
- ✅ Professional registration form
- ✅ Tenant data isolation
- ✅ Scalable architecture
- ✅ Security best practices

### Technologies

- **Backend**: Spring Boot, Spring Cloud, MySQL
- **Frontend**: React, React Router, Tailwind CSS
- **Database**: MySQL with Flyway migrations
- **Security**: JWT, Spring Security
- **Architecture**: Microservices, API Gateway

## 6. Production Considerations

### What You Would Need for Real Domains

1. **DNS Configuration**

   - Wildcard DNS record: `*.crmplatform.com`
   - SSL certificates for each subdomain

2. **Load Balancer**

   - Route subdomains to appropriate services
   - SSL termination

3. **SSL Certificates**

   - Wildcard certificate for `*.crmplatform.com`
   - Automatic certificate renewal

4. **Monitoring**
   - Subdomain health checks
   - Performance monitoring per tenant

### Why This Approach is Perfect for Internship

1. **Easy to Demo** - No complex infrastructure needed
2. **Shows Skills** - Full-stack development, architecture
3. **Scalable** - Can handle thousands of tenants
4. **Professional** - Industry-standard patterns
5. **Extensible** - Easy to add real domains later

## 7. Troubleshooting

### Common Issues

1. **Services not starting**: Check ports are available
2. **Database connection**: Ensure MySQL is running
3. **Frontend not loading**: Check npm dependencies
4. **CORS errors**: Verify API Gateway configuration

### Quick Fixes

```bash
# Kill processes on ports
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Reset database
mysql -u root -p
DROP DATABASE crm_contacts;
CREATE DATABASE crm_contacts;
```

## Conclusion

This setup allows you to demonstrate a professional, production-ready CRM platform without needing real domains. The subdomain simulation shows the same functionality while being easy to set up and demo.

**Key Benefits for Your Internship:**

- ✅ No infrastructure costs
- ✅ Easy to set up and demo
- ✅ Shows full-stack skills
- ✅ Demonstrates architecture knowledge
- ✅ Professional presentation
