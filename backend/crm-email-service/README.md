# CRM Email Service

A comprehensive email service microservice for the CRM Platform that handles all email communications including welcome emails, notifications, and marketing campaigns.

## Features

- **Multi-tenant Email Support**: Tenant-specific email configurations and templates
- **Async Email Processing**: Non-blocking email sending with queue management
- **Template Engine**: Thymeleaf-based HTML email templates with variable substitution
- **Email Logging**: Complete audit trail of all sent emails
- **Fallback Handling**: Graceful degradation when email service is unavailable
- **Multiple Email Types**: Welcome, notifications, marketing, system alerts, etc.

## Architecture

The email service follows the microservices pattern and integrates with:
- **Eureka Server**: Service discovery and registration
- **API Gateway**: Routing and load balancing
- **Auth Service**: Welcome email integration
- **RabbitMQ**: Message queue for async processing
- **MySQL**: Email logs and template storage

## Configuration

### Email Provider Setup

Configure your email provider in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME:your-email@gmail.com}
    password: ${EMAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Environment Variables

Set these environment variables for production:
- `EMAIL_USERNAME`: Your SMTP username
- `EMAIL_PASSWORD`: Your SMTP password (use app passwords for Gmail)

## API Endpoints

### Send Email
```http
POST /api/email/send
Content-Type: application/json

{
  "tenantId": "tenant123",
  "toEmail": "user@example.com",
  "subject": "Test Email",
  "content": "Email content",
  "emailType": "NOTIFICATION",
  "isHtml": true
}
```

### Send Async Email
```http
POST /api/email/send-async
Content-Type: application/json

{
  "tenantId": "tenant123",
  "toEmail": "user@example.com",
  "subject": "Test Email",
  "templateName": "WELCOME",
  "templateVariables": {
    "userName": "John Doe",
    "tenantName": "Acme Corp"
  },
  "emailType": "WELCOME"
}
```

### Send Welcome Email
```http
POST /api/email/welcome?tenantId=tenant123&toEmail=user@example.com&userName=John Doe&tenantName=Acme Corp
```

## Database Schema

### Email Templates
- `id`: Primary key
- `tenant_id`: Tenant identifier
- `template_name`: Unique template name
- `subject`: Email subject
- `html_content`: HTML template content
- `text_content`: Plain text fallback
- `template_type`: Template category
- `is_active`: Template status

### Email Logs
- `id`: Primary key
- `tenant_id`: Tenant identifier
- `from_email`: Sender email
- `to_email`: Recipient email
- `subject`: Email subject
- `status`: Delivery status
- `email_type`: Email category
- `sent_at`: Timestamp

## Integration with Other Services

### Auth Service Integration

The email service is integrated with the auth service for welcome emails:

```java
@Autowired
private EmailServiceClient emailServiceClient;

private void sendWelcomeEmail(Tenant tenant, User adminUser) {
    emailServiceClient.sendWelcomeEmail(
        tenant.getTenantId(),
        adminUser.getEmail(),
        adminUser.getFirstName() + " " + adminUser.getLastName(),
        tenant.getTenantName()
    );
}
```

## Email Templates

### Welcome Email Template
Located at `src/main/resources/templates/welcome-email.html`

Features:
- Responsive design
- Modern styling
- Tenant branding
- Call-to-action buttons
- Getting started guide

### Template Variables
- `userName`: Recipient's full name
- `tenantName`: Organization name
- `loginUrl`: Platform login URL

## Monitoring and Logging

### Health Checks
- `/actuator/health`: Service health status
- `/actuator/metrics`: Performance metrics

### Logging Levels
```yaml
logging:
  level:
    com.crmplatform.email: DEBUG
    org.springframework.mail: DEBUG
```

## Error Handling

- **Service Unavailable**: Fallback client handles graceful degradation
- **Email Failures**: Logged with error details for retry processing
- **Template Missing**: Falls back to default templates

## Security Considerations

- Email credentials stored as environment variables
- Tenant isolation for templates and logs
- Input validation on all endpoints
- Rate limiting (configure in API Gateway)

## Development Setup

1. **Start Dependencies**:
   ```bash
   # Start MySQL, RabbitMQ, and Eureka Server
   ```

2. **Configure Email**:
   ```bash
   set EMAIL_USERNAME=your-email@gmail.com
   set EMAIL_PASSWORD=your-app-password
   ```

3. **Run Service**:
   ```bash
   cd crm-email-service
   mvn spring-boot:run
   ```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn integration-test
```

## Production Deployment

1. **Environment Variables**: Set production email credentials
2. **Database**: Configure production MySQL instance
3. **Message Queue**: Set up RabbitMQ cluster
4. **Monitoring**: Enable metrics and health checks
5. **Scaling**: Configure multiple instances behind load balancer

## Troubleshooting

### Common Issues

1. **Email Not Sending**:
   - Check SMTP credentials
   - Verify firewall settings
   - Check application logs

2. **Template Not Found**:
   - Verify template exists in database
   - Check tenant ID mapping
   - Review template activation status

3. **Service Discovery Issues**:
   - Ensure Eureka Server is running
   - Check service registration
   - Verify network connectivity

## Future Enhancements

- Email scheduling and campaigns
- Advanced template editor
- Email analytics and tracking
- Bounce handling and suppression lists
- Multi-language template support
