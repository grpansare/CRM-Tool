-- Create leads table
CREATE TABLE leads (
    lead_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(50),
    company VARCHAR(255),
    job_title VARCHAR(100),
    lead_source VARCHAR(50),
    lead_status VARCHAR(20) DEFAULT 'NEW',
    lead_score INT DEFAULT 0,
    notes TEXT,
    owner_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (lead_status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'UNQUALIFIED', 'NURTURING', 'CONVERTED', 'LOST')),
    CHECK (lead_source IN ('WEBSITE', 'SOCIAL_MEDIA', 'EMAIL_CAMPAIGN', 'COLD_CALL', 'REFERRAL', 'TRADE_SHOW', 'WEBINAR', 'CONTENT_DOWNLOAD', 'ADVERTISEMENT', 'PARTNER', 'OTHER')),
    CHECK (lead_score >= 0 AND lead_score <= 100)
);

-- Create indexes for better performance
CREATE INDEX idx_leads_tenant_id ON leads(tenant_id);
CREATE INDEX idx_leads_owner_user_id ON leads(owner_user_id);
CREATE INDEX idx_leads_email ON leads(email);
CREATE INDEX idx_leads_status ON leads(lead_status);
CREATE INDEX idx_leads_source ON leads(lead_source);
CREATE INDEX idx_leads_created_at ON leads(created_at);

-- Insert sample leads for testing
INSERT INTO leads (tenant_id, first_name, last_name, email, phone_number, company, job_title, lead_source, lead_status, lead_score, owner_user_id) VALUES 
(1, 'John', 'Doe', 'john.doe@example.com', '+1-555-0101', 'Tech Corp', 'CTO', 'WEBSITE', 'NEW', 85, 1),
(1, 'Jane', 'Smith', 'jane.smith@business.com', '+1-555-0102', 'Business Inc', 'VP Sales', 'REFERRAL', 'CONTACTED', 75, 1),
(1, 'Bob', 'Johnson', 'bob.johnson@startup.io', '+1-555-0103', 'Startup LLC', 'Founder', 'SOCIAL_MEDIA', 'QUALIFIED', 90, 1);
