-- Lead Assignment Rules Table
CREATE TABLE lead_assignment_rules (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    priority_order INT DEFAULT 0,
    
    -- Rule Conditions (JSON or separate condition tables)
    conditions JSON,
    
    -- Assignment Strategy
    assignment_strategy ENUM('ROUND_ROBIN', 'LOAD_BALANCED', 'TERRITORY_BASED', 'SKILL_BASED', 'RANDOM') DEFAULT 'ROUND_ROBIN',
    
    -- Target Users/Teams
    assigned_user_ids JSON, -- Array of user IDs
    assigned_team_ids JSON, -- Array of team IDs
    
    -- Metadata
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX idx_priority (priority_order),
    INDEX idx_created_at (created_at)
);

-- Lead Assignment History Table
CREATE TABLE lead_assignment_history (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    lead_id BIGINT NOT NULL,
    rule_id BIGINT,
    
    -- Assignment Details
    assigned_from_user_id BIGINT,
    assigned_to_user_id BIGINT NOT NULL,
    assignment_reason VARCHAR(500),
    assignment_method ENUM('MANUAL', 'AUTOMATIC', 'RULE_BASED') DEFAULT 'AUTOMATIC',
    
    -- Metadata
    assigned_by BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_lead (tenant_id, lead_id),
    INDEX idx_assigned_to (assigned_to_user_id),
    INDEX idx_assigned_at (assigned_at),
    
    FOREIGN KEY (lead_id) REFERENCES leads(lead_id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES lead_assignment_rules(rule_id) ON DELETE SET NULL
);

-- User Workload Tracking Table
CREATE TABLE user_lead_workload (
    workload_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- Workload Metrics
    active_leads_count INT DEFAULT 0,
    total_leads_assigned INT DEFAULT 0,
    leads_converted_this_month INT DEFAULT 0,
    average_response_time_hours DECIMAL(10,2),
    
    -- Capacity Settings
    max_lead_capacity INT DEFAULT 50,
    is_available BOOLEAN DEFAULT TRUE,
    availability_hours JSON, -- Working hours configuration
    
    -- Performance Metrics
    conversion_rate DECIMAL(5,2) DEFAULT 0.00,
    last_activity_at TIMESTAMP,
    
    -- Metadata
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_tenant_user (tenant_id, user_id),
    INDEX idx_tenant_available (tenant_id, is_available),
    INDEX idx_workload (active_leads_count)
);

-- Lead Routing Queue Table
CREATE TABLE lead_routing_queue (
    queue_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    lead_id BIGINT NOT NULL,
    
    -- Queue Status
    queue_status ENUM('PENDING', 'PROCESSING', 'ASSIGNED', 'FAILED') DEFAULT 'PENDING',
    priority_score INT DEFAULT 0,
    
    -- Routing Attempts
    routing_attempts INT DEFAULT 0,
    max_attempts INT DEFAULT 3,
    last_attempt_at TIMESTAMP,
    failure_reason TEXT,
    
    -- Assignment Result
    assigned_to_user_id BIGINT,
    assigned_rule_id BIGINT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_status (tenant_id, queue_status),
    INDEX idx_priority (priority_score DESC),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (lead_id) REFERENCES leads(lead_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_rule_id) REFERENCES lead_assignment_rules(rule_id) ON DELETE SET NULL
);

-- Email Templates Table
CREATE TABLE email_templates (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type ENUM('LEAD_WELCOME', 'FOLLOW_UP', 'NURTURE', 'MEETING_INVITE', 'PROPOSAL', 'CUSTOM') NOT NULL,
    
    -- Email Content
    subject_line VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    html_body TEXT,
    
    -- Template Variables
    available_variables JSON, -- List of available merge fields
    
    -- Settings
    is_active BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_type (tenant_id, template_type),
    INDEX idx_active (is_active),
    UNIQUE KEY unique_tenant_default_type (tenant_id, template_type, is_default)
);

-- Email Sent History Table
CREATE TABLE email_sent_history (
    email_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    lead_id BIGINT,
    contact_id BIGINT,
    template_id BIGINT,
    
    -- Email Details
    recipient_email VARCHAR(255) NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    subject_line VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    
    -- Send Status
    send_status ENUM('PENDING', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'FAILED', 'BOUNCED') DEFAULT 'PENDING',
    external_message_id VARCHAR(255), -- From email service provider
    
    -- Tracking
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    failed_reason TEXT,
    
    -- Metadata
    sent_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_lead (tenant_id, lead_id),
    INDEX idx_recipient (recipient_email),
    INDEX idx_status (send_status),
    INDEX idx_sent_at (sent_at),
    
    FOREIGN KEY (lead_id) REFERENCES leads(lead_id) ON DELETE SET NULL,
    FOREIGN KEY (template_id) REFERENCES email_templates(template_id) ON DELETE SET NULL
);

-- Insert Default Email Templates
INSERT INTO email_templates (tenant_id, template_name, template_type, subject_line, email_body, available_variables, is_default, created_by) VALUES
(1, 'Lead Welcome Email', 'LEAD_WELCOME', 
 'Welcome {{firstName}} - Thank you for your interest!', 
 'Hi {{firstName}},\n\nThank you for your interest in our services. We''re excited to help you achieve your goals.\n\nI''ll be reaching out soon to discuss how we can assist you.\n\nBest regards,\n{{senderName}}\n{{companyName}}',
 '["firstName", "lastName", "company", "senderName", "companyName", "email", "phone"]',
 TRUE, 1),

(1, 'Follow-up Email', 'FOLLOW_UP',
 'Following up on our conversation - {{firstName}}',
 'Hi {{firstName}},\n\nI wanted to follow up on our recent conversation about {{company}}''s needs.\n\nDo you have time this week for a quick call to discuss next steps?\n\nBest regards,\n{{senderName}}',
 '["firstName", "lastName", "company", "senderName", "companyName", "email", "phone"]',
 TRUE, 1),

(1, 'Meeting Invitation', 'MEETING_INVITE',
 'Let''s schedule a meeting - {{firstName}}',
 'Hi {{firstName}},\n\nI''d love to schedule a meeting to discuss how we can help {{company}} achieve its goals.\n\nAre you available for a 30-minute call this week?\n\nPlease let me know what works best for you.\n\nBest regards,\n{{senderName}}',
 '["firstName", "lastName", "company", "senderName", "companyName", "email", "phone"]',
 TRUE, 1);

-- Insert Default Assignment Rule
INSERT INTO lead_assignment_rules (tenant_id, rule_name, description, conditions, assignment_strategy, assigned_user_ids, created_by) VALUES
(1, 'Default Round Robin', 'Default rule to assign leads in round-robin fashion to all active sales reps', 
 '{"leadSource": ["WEBSITE", "SOCIAL_MEDIA"], "leadScore": {"min": 0, "max": 100}}',
 'ROUND_ROBIN', 
 '[1, 2, 3]', -- Default user IDs - adjust based on your users
 1);
