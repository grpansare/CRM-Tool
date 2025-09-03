# Sales Pipeline Service Testing Guide

## Prerequisites
- MySQL running on port 3306
- All CRM services started (see startup commands below)
- Valid JWT token for authentication

## Service Startup Order
1. Eureka Server (8761)
2. API Gateway (8080)
3. Auth Service (8083)
4. Contacts Service (8081)
5. Sales Service (8082)
6. Frontend (3000)

## Test Scenarios

### 1. Authentication Setup
First, get a JWT token:

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-email@example.com",
    "password": "your-password"
  }'
```

Save the returned token for subsequent requests.

### 2. Pipeline Management Tests

#### Create a Pipeline
```bash
curl -X POST http://localhost:8080/api/v1/pipelines \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "pipelineName": "Q1 2024 Sales Pipeline"
  }'
```

#### Get All Pipelines
```bash
curl -X GET http://localhost:8080/api/v1/pipelines \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Pipeline by ID
```bash
curl -X GET http://localhost:8080/api/v1/pipelines/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Deal Management Tests

#### Create a Deal
```bash
curl -X POST http://localhost:8080/api/v1/deals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "dealName": "Acme Corp Software License",
    "amount": 50000.00,
    "expectedCloseDate": "2024-12-31",
    "stageId": 1,
    "contactId": 1,
    "accountId": 1
  }'
```

#### Get My Deals
```bash
curl -X GET http://localhost:8080/api/v1/deals \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Update Deal Stage (Key Feature)
```bash
curl -X PUT http://localhost:8080/api/v1/deals/1/stage \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "newStageId": 2
  }'
```

### 4. Stage Management Tests

#### Create Custom Stage
```bash
curl -X POST http://localhost:8080/api/v1/pipelines/1/stages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "stageName": "Demo Scheduled",
    "stageOrder": 3,
    "stageType": "OPEN",
    "winProbability": 40.00
  }'
```

#### Update Stage
```bash
curl -X PUT http://localhost:8080/api/v1/pipelines/stages/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "stageName": "Qualified Lead",
    "stageOrder": 2,
    "stageType": "OPEN", 
    "winProbability": 30.00
  }'
```

## Frontend Testing Steps

### 1. Login and Navigation
1. Open http://localhost:3000
2. Login with valid credentials
3. Navigate to "Deals" section

### 2. Pipeline Visualization
- View deals organized by pipeline stages
- Verify stage names and deal counts
- Check total pipeline value calculations

### 3. Deal Creation
1. Click "Create Deal" button
2. Fill in required fields:
   - Deal Name
   - Amount
   - Expected Close Date
   - Select Contact and Account
   - Choose Initial Stage
3. Submit and verify creation

### 4. Stage Movement (Core Feature)
1. Find a deal in the pipeline
2. Drag and drop to different stage OR
3. Use stage dropdown to move deal
4. Verify stage history is tracked
5. Check time calculations

### 5. Pipeline Management
1. Access pipeline settings
2. Create new pipeline
3. Add/modify stages
4. Set win probabilities
5. Reorder stages

## Expected Results

### Database Verification
Check these tables for data:
- `sales_pipelines` - Pipeline records
- `pipeline_stages` - Stage definitions with win probabilities
- `deals` - Deal records with current stage
- `deal_stage_history` - Stage change audit trail

### Key Features to Verify
✅ Deal creation with stage assignment
✅ Stage movement with history tracking
✅ Time calculation between stages
✅ Win probability calculations
✅ Multi-tenant data isolation
✅ Permission validation (deal ownership)
✅ Pipeline value aggregations

## Troubleshooting

### Common Issues
1. **401 Unauthorized** - Check JWT token validity
2. **404 Not Found** - Verify service is running on correct port
3. **403 Forbidden** - Check user permissions and tenant context
4. **500 Internal Server Error** - Check service logs for details

### Service Health Checks
```bash
# Check service health
curl http://localhost:8082/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

### Log Monitoring
Monitor logs in service terminals for:
- SQL queries execution
- JWT token validation
- Stage change events
- Error messages

## Success Criteria
- ✅ Pipelines created with default stages
- ✅ Deals created and assigned to stages  
- ✅ Stage movements tracked in history
- ✅ Time calculations accurate
- ✅ Frontend displays pipeline visually
- ✅ All CRUD operations working
- ✅ Multi-tenant isolation maintained
