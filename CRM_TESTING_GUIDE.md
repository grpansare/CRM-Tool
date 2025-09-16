# CRM Platform Testing Guide

## Complete Testing Data & Scenarios

This guide provides comprehensive test data and step-by-step instructions to test all CRM functionality from lead creation to account conversion.

---

## 1. PREREQUISITES & SETUP

### 1.1 Services to Start
```bash
# Start all services in order:
1. crm-auth-service (Port 8081)
2. crm-contacts-service (Port 8082) 
3. crm-activity-service (Port 8084)
4. crm-email-service (Port 8085)
5. crm-sales-service (Port 8083)
6. crm-api-gateway (Port 8080)
7. Frontend React app (Port 3000)
```

### 1.2 Test User Account
```json
{
  "email": "admin@testcompany.com",
  "password": "Admin123!",
  "firstName": "Test",
  "lastName": "Admin",
  "role": "ADMIN"
}
```

---

## 2. LEAD SCORING TEST DATA

### 2.1 High Score Lead (Grade A: 80-100 points)
```json
{
  "firstName": "John",
  "lastName": "Smith", 
  "email": "john.smith@techcorp.com",
  "phoneNumber": "+1-555-0123",
  "company": "TechCorp Solutions Inc",
  "jobTitle": "CEO",
  "leadSource": "REFERRAL",
  "leadStatus": "NEW",
  "notes": "Very interested in our enterprise solution. Has budget approved and decision-making authority. Mentioned they need to implement by Q4. Company has 500+ employees and looking for comprehensive CRM platform."
}
```
**Expected Score**: 85-95 points (Grade A)
- Demographic: High (business email, CEO title, complete info)
- Firmographic: High (established company, senior decision maker)
- Behavioral: High (detailed notes, phone provided)
- Source: High (referral source)

### 2.2 Medium Score Lead (Grade B: 60-79 points)
```json
{
  "firstName": "Sarah",
  "lastName": "Johnson",
  "email": "sarah.johnson@midsize-corp.com",
  "phoneNumber": "+1-555-0456",
  "company": "Midsize Corp",
  "jobTitle": "Marketing Director",
  "leadSource": "WEBSITE",
  "leadStatus": "CONTACTED",
  "notes": "Interested in marketing automation features."
}
```
**Expected Score**: 65-75 points (Grade B)

### 2.3 Low Score Lead (Grade C: 40-59 points)
```json
{
  "firstName": "Mike",
  "lastName": "Wilson",
  "email": "mike.wilson@gmail.com",
  "company": "Small Business",
  "jobTitle": "Owner",
  "leadSource": "SOCIAL_MEDIA",
  "leadStatus": "NEW",
  "notes": "Basic inquiry"
}
```
**Expected Score**: 45-55 points (Grade C)

### 2.4 Poor Score Lead (Grade D: 0-39 points)
```json
{
  "firstName": "Anonymous",
  "lastName": "User",
  "email": "test@yahoo.com",
  "leadSource": "OTHER",
  "leadStatus": "NEW"
}
```
**Expected Score**: 15-25 points (Grade D)

---

## 3. STEP-BY-STEP TESTING SCENARIOS

### 3.1 Lead Creation & Scoring Test

**Step 1: Create High Score Lead**
```bash
POST http://localhost:8080/api/v1/leads
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith", 
  "email": "john.smith@techcorp.com",
  "phoneNumber": "+1-555-0123",
  "company": "TechCorp Solutions Inc",
  "jobTitle": "CEO",
  "leadSource": "REFERRAL",
  "leadStatus": "NEW",
  "notes": "Very interested in our enterprise solution. Has budget approved and decision-making authority."
}
```

**Expected Response:**
```json
{
  "leadId": 1,
  "firstName": "John",
  "lastName": "Smith",
  "leadScore": 87,
  "scoreGrade": "A",
  "leadStatus": "NEW"
}
```

**Step 2: Get Score Breakdown**
```bash
GET http://localhost:8080/api/v1/leads/1/score-breakdown
Authorization: Bearer {jwt_token}
```

**Expected Response:**
```json
{
  "leadId": 1,
  "totalScore": 87,
  "grade": "A",
  "gradeDescription": "Hot lead - immediate follow-up required",
  "scoreBreakdown": {
    "demographicScore": 26,
    "firmographicScore": 22,
    "behavioralScore": 21,
    "sourceScore": 18
  }
}
```

### 3.2 Lead Update & Score Recalculation Test

**Step 1: Update Lead Status**
```bash
PUT http://localhost:8080/api/v1/leads/1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "leadStatus": "QUALIFIED",
  "notes": "Updated notes after qualification call. Very promising prospect with immediate need."
}
```

**Step 2: Recalculate Score**
```bash
POST http://localhost:8080/api/v1/leads/1/recalculate-score
Authorization: Bearer {jwt_token}
```

### 3.3 Lead Conversion Test

**Step 1: Convert Lead to Contact & Account**
```bash
POST http://localhost:8080/api/v1/leads/1/convert
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "createAccount": true,
  "accountName": "TechCorp Solutions Inc",
  "dealName": "TechCorp CRM Implementation",
  "dealValue": 50000.00,
  "dealStage": "QUALIFICATION"
}
```

**Expected Response:**
```json
{
  "success": true,
  "contactId": 101,
  "accountId": 201,
  "dealId": 301,
  "message": "Lead converted successfully"
}
```

### 3.4 Duplicate Account Handling Test

**Step 1: Create Second Lead with Same Company**
```bash
POST http://localhost:8080/api/v1/leads
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@techcorp.com",
  "company": "TechCorp Solutions Inc",
  "jobTitle": "CTO",
  "leadSource": "REFERRAL"
}
```

**Step 2: Convert Second Lead (Should Use Existing Account)**
```bash
POST http://localhost:8080/api/v1/leads/2/convert
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "createAccount": true,
  "accountName": "TechCorp Solutions Inc",
  "dealName": "TechCorp Additional Services",
  "dealValue": 25000.00
}
```

**Expected**: Should return existing accountId (201) instead of creating duplicate.

---

## 4. FRONTEND TESTING SCENARIOS

### 4.1 Lead Management UI Test

**Test Steps:**
1. Login to frontend (http://localhost:3000)
2. Navigate to Leads page
3. Click "Add New Lead" button
4. Fill form with High Score Lead data
5. Submit and verify lead appears in list
6. Check lead score display (should show stars and grade)
7. Click on lead to view details
8. Verify score breakdown is displayed

### 4.2 Lead Conversion UI Test

**Test Steps:**
1. Open lead details for qualified lead
2. Click "Convert Lead" button
3. Fill conversion form:
   - Check "Create Account"
   - Enter account name
   - Enter deal details
4. Submit conversion
5. Verify success message
6. Check that lead status changed to "CONVERTED"
7. Navigate to Contacts to verify contact created
8. Navigate to Accounts to verify account created

### 4.3 Activity Timeline Test

**Test Steps:**
1. Create lead and perform several actions:
   - Update lead status
   - Add notes
   - Recalculate score
   - Convert lead
2. View activity timeline
3. Verify all activities are logged with timestamps
4. Check activity descriptions are meaningful

---

## 5. API ENDPOINT TESTING

### 5.1 Lead Management Endpoints

```bash
# Get all leads
GET http://localhost:8080/api/v1/leads?page=0&size=10

# Get lead by ID
GET http://localhost:8080/api/v1/leads/1

# Search leads
GET http://localhost:8080/api/v1/leads/search?query=TechCorp

# Get leads by status
GET http://localhost:8080/api/v1/leads/status/NEW

# Get leads by source
GET http://localhost:8080/api/v1/leads/source/REFERRAL

# Update lead
PUT http://localhost:8080/api/v1/leads/1

# Delete lead
DELETE http://localhost:8080/api/v1/leads/1
```

### 5.2 Lead Scoring Endpoints

```bash
# Get score breakdown
GET http://localhost:8080/api/v1/leads/1/score-breakdown

# Recalculate score
POST http://localhost:8080/api/v1/leads/1/recalculate-score
```

### 5.3 Lead Conversion Endpoints

```bash
# Convert lead
POST http://localhost:8080/api/v1/leads/1/convert

# Get conversion history
GET http://localhost:8080/api/v1/leads/conversions
```

---

## 6. ERROR SCENARIO TESTING

### 6.1 Validation Error Tests

**Test Invalid Email:**
```json
{
  "firstName": "Test",
  "lastName": "User",
  "email": "invalid-email",
  "company": "Test Corp"
}
```
**Expected**: 400 Bad Request with validation error

**Test Duplicate Email:**
```json
{
  "firstName": "Duplicate",
  "lastName": "User", 
  "email": "john.smith@techcorp.com"
}
```
**Expected**: 409 Conflict - Email already exists

### 6.2 Service Communication Error Tests

**Test with Contacts Service Down:**
1. Stop crm-contacts-service
2. Try to convert a lead
3. **Expected**: Graceful error handling with appropriate message

**Test with Activity Service Down:**
1. Stop crm-activity-service  
2. Create/update leads
3. **Expected**: Lead operations succeed, activity logging fails gracefully

---

## 7. PERFORMANCE TESTING

### 7.1 Bulk Lead Creation Test

**Create 100 leads with script:**
```bash
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/leads \
    -H "Authorization: Bearer {token}" \
    -H "Content-Type: application/json" \
    -d "{
      \"firstName\": \"Test$i\",
      \"lastName\": \"User$i\",
      \"email\": \"test$i@example.com\",
      \"company\": \"Company $i\"
    }"
done
```

### 7.2 Pagination Test

**Test large dataset pagination:**
```bash
# Get first page
GET http://localhost:8080/api/v1/leads?page=0&size=20

# Get specific page
GET http://localhost:8080/api/v1/leads?page=5&size=20

# Test sorting
GET http://localhost:8080/api/v1/leads?page=0&size=20&sort=leadScore,desc
```

---

## 8. INTEGRATION TESTING CHECKLIST

### 8.1 Cross-Service Integration
- [ ] Lead creation triggers activity logging
- [ ] Lead conversion creates contact in contacts service
- [ ] Account creation handles duplicates properly
- [ ] Email workflows triggered on lead status changes
- [ ] JWT authentication works across all services

### 8.2 Database Integration
- [ ] Lead data persisted correctly
- [ ] Tenant isolation working
- [ ] Foreign key relationships maintained
- [ ] Database transactions handle failures

### 8.3 Frontend Integration
- [ ] API calls work from frontend
- [ ] Error messages displayed properly
- [ ] Real-time updates working
- [ ] Form validations working

---

## 9. EXPECTED RESULTS SUMMARY

### 9.1 Lead Scoring Results
- **High Score Lead**: 80-95 points (Grade A)
- **Medium Score Lead**: 60-75 points (Grade B)  
- **Low Score Lead**: 40-55 points (Grade C)
- **Poor Score Lead**: 15-25 points (Grade D)

### 9.2 Conversion Results
- Contact created in contacts service
- Account created or existing account linked
- Deal/opportunity created
- Lead status updated to CONVERTED
- Activities logged throughout process

### 9.3 Error Handling Results
- Validation errors return 400 with clear messages
- Duplicate detection returns 409 with existing record info
- Service failures handled gracefully
- Database constraints enforced

---

## 10. TROUBLESHOOTING GUIDE

### 10.1 Common Issues
- **JWT Token Expired**: Re-authenticate to get new token
- **Service Not Responding**: Check service logs and restart if needed
- **Database Connection**: Verify database is running and accessible
- **CORS Issues**: Check API Gateway CORS configuration

### 10.2 Log Locations
- **Sales Service**: `backend/crm-sales-service/logs/`
- **Contacts Service**: `backend/crm-contacts-service/logs/`
- **Activity Service**: `backend/crm-activity-service/logs/`
- **Frontend Console**: Browser Developer Tools

This comprehensive testing guide ensures all CRM functionality is thoroughly validated from basic CRUD operations to complex cross-service integrations.
