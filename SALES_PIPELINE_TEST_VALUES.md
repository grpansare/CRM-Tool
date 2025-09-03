# Sales Pipeline Service - Test Values & Scenarios

## 🔧 Fixed Issue
**Problem**: 400 error when moving deals between stages
**Solution**: Changed `stageId` to `newStageId` in Pipeline.jsx line 120

## 📊 Test Data Values

### 1. ✅ Create Pipeline - Auto-generates 6 default stages

**Test Pipeline Names:**
```json
{
  "pipelineName": "Q1 2024 Enterprise Sales"
}

{
  "pipelineName": "SMB Outbound Pipeline"
}

{
  "pipelineName": "Inbound Lead Pipeline"
}
```

**Expected Default Stages Created:**
1. **Prospecting** (Order: 1, Win Rate: 10%, Type: OPEN)
2. **Qualification** (Order: 2, Win Rate: 25%, Type: OPEN)
3. **Proposal** (Order: 3, Win Rate: 50%, Type: OPEN)
4. **Negotiation** (Order: 4, Win Rate: 75%, Type: OPEN)
5. **Closed Won** (Order: 5, Win Rate: 100%, Type: WON)
6. **Closed Lost** (Order: 6, Win Rate: 0%, Type: LOST)

### 2. ✅ Create Deals - Assign to pipeline stages

**Sample Deal Data:**
```json
{
  "dealName": "Acme Corp CRM License",
  "amount": 75000.00,
  "expectedCloseDate": "2024-12-15",
  "stageId": 1,
  "contactId": 1,
  "accountId": 1
}

{
  "dealName": "TechStart Software Implementation",
  "amount": 125000.00,
  "expectedCloseDate": "2024-11-30",
  "stageId": 2,
  "contactId": 2,
  "accountId": 2
}

{
  "dealName": "Global Enterprises Integration",
  "amount": 250000.00,
  "expectedCloseDate": "2025-01-31",
  "stageId": 3,
  "contactId": 3,
  "accountId": 3
}

{
  "dealName": "StartupXYZ Basic Package",
  "amount": 25000.00,
  "expectedCloseDate": "2024-10-15",
  "stageId": 1,
  "contactId": 4,
  "accountId": 4
}

{
  "dealName": "MegaCorp Enterprise Suite",
  "amount": 500000.00,
  "expectedCloseDate": "2025-03-31",
  "stageId": 4,
  "contactId": 5,
  "accountId": 5
}
```

### 3. ✅ Move Deals - Drag between stages or use dropdown

**Stage Movement Test Scenarios:**

**Scenario A: Progressive Movement**
- Move "Acme Corp CRM License" from Prospecting (1) → Qualification (2)
- Expected: History entry with ~0-1 days in previous stage

**Scenario B: Skip Stages**
- Move "TechStart Software Implementation" from Qualification (2) → Negotiation (4)
- Expected: History entry with calculated days in Qualification stage

**Scenario C: Backward Movement**
- Move "Global Enterprises Integration" from Proposal (3) → Qualification (2)
- Expected: History entry tracking the backward movement

**API Call Format:**
```json
PUT /api/v1/deals/{dealId}/stage
{
  "newStageId": 2
}
```

### 4. ✅ Stage History - Track time spent in each stage

**Expected History Tracking:**

After moving deals, check `deal_stage_history` table for:
```sql
-- Example history entries
INSERT INTO deal_stage_history VALUES
(1, 1, 1, 1, 2, '2024-08-25 16:47:10', 1),  -- 1 day in Prospecting
(2, 1, 2, 2, 4, '2024-08-25 16:50:00', 3),  -- 3 days in Qualification
(3, 1, 3, 3, 2, '2024-08-25 16:55:00', 2);  -- 2 days in Proposal (moved back)
```

**Time Calculation Test:**
- Create deal at 10:00 AM
- Move to next stage at 2:00 PM same day = 0 days
- Move again next day at 11:00 AM = 1 day
- Move again 3 days later = 3 days

### 5. ✅ Pipeline Analytics - View totals and win probabilities

**Expected Analytics Results:**

**Pipeline: "Q1 2024 Enterprise Sales"**
- **Total Deals**: 5
- **Total Pipeline Value**: $975,000
- **Active Stages**: 4 (OPEN type stages)

**Stage-wise Breakdown:**
- **Prospecting**: 2 deals, $100,000, 10% win rate
- **Qualification**: 1 deal, $125,000, 25% win rate  
- **Proposal**: 1 deal, $250,000, 50% win rate
- **Negotiation**: 1 deal, $500,000, 75% win rate
- **Closed Won**: 0 deals, $0, 100% win rate
- **Closed Lost**: 0 deals, $0, 0% win rate

**Weighted Pipeline Value Calculation:**
- Prospecting: $100,000 × 10% = $10,000
- Qualification: $125,000 × 25% = $31,250
- Proposal: $250,000 × 50% = $125,000
- Negotiation: $500,000 × 75% = $375,000
- **Total Weighted Value**: $541,250

## 🧪 Complete Test Sequence

### Step 1: Create Pipeline
1. Navigate to Pipeline page
2. Click "New Pipeline"
3. Enter: "Q1 2024 Enterprise Sales"
4. Verify 6 default stages created

### Step 2: Create Test Deals
1. Navigate to Deals page
2. Create 5 deals using sample data above
3. Assign to different stages (1,1,2,3,4)

### Step 3: Test Stage Movement
1. Drag "Acme Corp" from Prospecting to Qualification
2. Use dropdown to move "TechStart" to Negotiation
3. Verify toast notifications show success

### Step 4: Verify Analytics
1. Check pipeline totals update
2. Verify stage counts and values
3. Check win probability calculations

### Step 5: Verify History
1. Move deals multiple times
2. Check time calculations are accurate
3. Verify all movements tracked

## 🎯 Success Criteria

- ✅ Pipeline created with 6 default stages
- ✅ Deals created and assigned to stages
- ✅ Drag-and-drop movement works
- ✅ Stage history tracks time accurately
- ✅ Analytics show correct totals
- ✅ Win probabilities calculated properly
- ✅ Multi-tenant data isolation maintained

## 🔍 Debugging Tips

**If 400 errors occur:**
- Check request payload matches DTO exactly
- Verify JWT token is valid
- Check tenant context is set
- Ensure required fields are provided

**If drag-and-drop fails:**
- Check console for JavaScript errors
- Verify API endpoints are accessible
- Check network tab for failed requests
- Ensure proper authentication headers
