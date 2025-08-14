# CRM Activity Service

The Activity Service is responsible for managing activities and timeline functionality in the CRM platform. It captures every interaction with contacts, accounts, and deals, providing a complete audit trail and timeline view.

## Features

- **Activity Logging**: Log various types of activities (CALL, EMAIL, MEETING, NOTE, TASK)
- **Timeline Views**: Retrieve chronological timelines for contacts, accounts, and deals
- **Event Publishing**: Publishes Activity.Created events for automation and analytics
- **Multi-tenant Support**: Complete data isolation between tenants
- **MongoDB Storage**: Uses MongoDB for high-volume, unstructured activity data

## API Endpoints

### Create Activity
```http
POST /api/v1/activities
Content-Type: application/json

{
  "type": "CALL",
  "content": "Called John Smith about the proposal. He's interested in the enterprise package.",
  "outcome": "Connected",
  "associations": {
    "contacts": [312, 315],
    "accounts": [58],
    "deals": [92]
  }
}
```

### Get Contact Timeline
```http
GET /api/v1/activities/contacts/{contactId}/timeline?page=0&size=20
```

### Get Account Timeline
```http
GET /api/v1/activities/accounts/{accountId}/timeline?page=0&size=20
```

### Get Deal Timeline
```http
GET /api/v1/activities/deals/{dealId}/timeline?page=0&size=20
```

### Get User Activities
```http
GET /api/v1/activities/my-activities?page=0&size=20
```

## Activity Types

- **CALL**: Phone calls with contacts
- **EMAIL**: Email communications
- **MEETING**: In-person or virtual meetings
- **NOTE**: General notes and observations
- **TASK**: Tasks and follow-ups

## Database Schema (MongoDB)

```javascript
{
  "_id": ObjectId("..."),
  "tenant_id": 101,
  "activity_id": "act_12345678",
  "user_id": 205,
  "type": "CALL",
  "timestamp": "2025-07-26T14:30:00Z",
  "content": "John is very interested in the enterprise package...",
  "outcome": "Connected",
  "associations": {
    "contacts": [312, 315],
    "accounts": [58],
    "deals": [92]
  }
}
```

## Configuration

### MongoDB
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=crm_activities
```

### RabbitMQ Events
```properties
crm.events.exchange=crm.events
crm.events.activity.created.routing-key=activity.created
```

## Running the Service

1. Ensure MongoDB is running on localhost:27017
2. Ensure RabbitMQ is running on localhost:5672
3. Start the service:
```bash
mvn spring-boot:run
```

The service will start on port 8084 and register with Eureka.

## Testing

Run unit tests:
```bash
mvn test
```

## Event Publishing

When an activity is created, the service publishes an `Activity.Created` event containing:
- Event type and timestamp
- Tenant and user information
- Activity details and associations
- Can be consumed by other services for automation and analytics
