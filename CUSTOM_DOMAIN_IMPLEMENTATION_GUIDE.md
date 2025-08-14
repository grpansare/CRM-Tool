# Custom Domain Implementation Guide for CRM Platform

## Overview

This guide explains how custom domains are implemented in the CRM platform for your internship project.

## Current Implementation: Subdomain Approach

### What We Have

- ✅ Subdomain-based URLs: `acme.crmplatform.com`
- ✅ Real-time availability checking
- ✅ Validation and reserved word protection
- ✅ Professional UI with radio button selection

### Frontend Features

1. **Dual Option Interface**

   - Custom Subdomain (Free) - Recommended
   - Custom Domain (Premium) - Disabled for demo

2. **Real-time Validation**

   - Checks availability as user types
   - Shows visual feedback (green checkmark/red error)
   - Prevents reserved subdomains

3. **Professional UI**
   - Radio button selection
   - Pricing badges (Free/Premium)
   - Live URL preview

### Backend Features

1. **Subdomain Validation**

   - Format validation (lowercase, numbers, hyphens only)
   - Length validation (3-63 characters)
   - Reserved word protection
   - Database uniqueness check

2. **API Endpoints**
   - `GET /api/v1/tenants/check-subdomain/{subdomain}`
   - `POST /api/v1/tenants/register`

## Alternative Approaches

### 1. Full Custom Domain (Advanced)

```javascript
// Example: crm.acmecorp.com
// Requires:
// - DNS configuration
// - SSL certificate management
// - Reverse proxy setup
// - Domain verification
```

### 2. Hybrid Approach (Production Ready)

```javascript
// Free Plan: acme.crmplatform.com
// Premium Plan: crm.acmecorp.com
// Enterprise: Full white-label solution
```

## Implementation Details

### Frontend Code Structure

```javascript
// TenantRegistration.jsx
const checkSubdomainAvailability = async (subdomain) => {
  const response = await fetch(`/api/v1/tenants/check-subdomain/${subdomain}`);
  const data = await response.json();
  setSubdomainAvailable(data.data);
};
```

### Backend Code Structure

```java
// TenantService.java
public boolean isSubdomainAvailable(String subdomain) {
  // 1. Format validation
  // 2. Length validation
  // 3. Reserved word check
  // 4. Database uniqueness check
  return !tenantRepository.existsBySubdomain(subdomain);
}
```

### Database Schema

```sql
-- tenants table
CREATE TABLE tenants (
  tenant_id INT AUTO_INCREMENT PRIMARY KEY,
  tenant_name VARCHAR(255) NOT NULL,
  subdomain VARCHAR(100) UNIQUE,
  custom_domain VARCHAR(255) UNIQUE,
  -- other fields...
);
```

## For Your Internship Demo

### What to Show

1. **Registration Flow**

   - User enters organization details
   - Chooses subdomain
   - Real-time availability check
   - Professional form validation

2. **Multi-tenancy**

   - Each tenant gets unique subdomain
   - Isolated data per tenant
   - Custom branding per tenant

3. **Technical Implementation**
   - Frontend validation
   - Backend API endpoints
   - Database design
   - Security considerations

### Demo Scenarios

```bash
# Scenario 1: Successful Registration
1. Go to /register
2. Fill organization details
3. Enter subdomain: "acme"
4. See real-time availability check
5. Complete registration
6. Access: acme.crmplatform.com

# Scenario 2: Subdomain Conflict
1. Try to register with existing subdomain
2. See error message
3. Try different subdomain
4. Success

# Scenario 3: Reserved Subdomain
1. Try to use "admin" as subdomain
2. See validation error
3. Choose valid subdomain
```

## Advanced Features (Optional)

### 1. Custom Domain Setup

```javascript
// For premium plans
const setupCustomDomain = async (domain) => {
  // 1. Verify domain ownership
  // 2. Configure DNS records
  // 3. Provision SSL certificate
  // 4. Update routing
};
```

### 2. Domain Verification

```javascript
// Verify domain ownership via DNS TXT record
const verifyDomain = async (domain) => {
  const txtRecord = await checkDNSTXTRecord(domain);
  return txtRecord.includes("crmplatform-verification");
};
```

### 3. SSL Certificate Management

```javascript
// Automatic SSL certificate provisioning
const provisionSSL = async (domain) => {
  // Use Let's Encrypt or similar service
  // Configure automatic renewal
};
```

## Security Considerations

### 1. Subdomain Validation

- Prevent SQL injection
- Validate format strictly
- Check for reserved words
- Rate limit availability checks

### 2. Domain Security

- Verify domain ownership
- Prevent subdomain takeover
- Implement proper CORS
- Use HTTPS everywhere

### 3. Multi-tenancy Security

- Isolate tenant data
- Validate tenant context
- Prevent cross-tenant access
- Audit all operations

## Testing Strategy

### Unit Tests

```java
@Test
public void testSubdomainValidation() {
  assertTrue(tenantService.isSubdomainAvailable("acme"));
  assertFalse(tenantService.isSubdomainAvailable("admin"));
  assertFalse(tenantService.isSubdomainAvailable("a"));
}
```

### Integration Tests

```java
@Test
public void testSubdomainRegistration() {
  // Test complete registration flow
  // Verify subdomain uniqueness
  // Check tenant isolation
}
```

### Frontend Tests

```javascript
test("subdomain availability check", async () => {
  // Test real-time validation
  // Test error handling
  // Test UI feedback
});
```

## Deployment Considerations

### 1. DNS Configuration

```nginx
# Nginx configuration for subdomain routing
server {
    listen 80;
    server_name *.crmplatform.com;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 2. SSL Certificates

```bash
# Wildcard SSL certificate for *.crmplatform.com
# Automatic certificate renewal
# HSTS headers
```

### 3. Load Balancing

```yaml
# Docker Compose with load balancer
services:
  nginx:
    image: nginx
    ports:
      - "80:80"
      - "443:443"
  backend:
    image: crm-backend
```

## Conclusion

For your internship project, the current subdomain approach is perfect because:

1. **Easy to Implement** - No complex DNS setup required
2. **Professional Looking** - Shows understanding of SaaS patterns
3. **Scalable** - Can handle thousands of tenants
4. **Secure** - Proper validation and isolation
5. **Demo-Friendly** - Easy to show working examples

The implementation demonstrates:

- ✅ Frontend development skills
- ✅ Backend API design
- ✅ Database design
- ✅ Security awareness
- ✅ User experience design
- ✅ Multi-tenancy architecture

This approach is used by many successful SaaS companies like:

- Slack (workspace.slack.com)
- Notion (workspace.notion.so)
- Linear (linear.app)
- Figma (figma.com)
