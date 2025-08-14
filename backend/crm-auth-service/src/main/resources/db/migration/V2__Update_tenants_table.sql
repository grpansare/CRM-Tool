-- Update tenants table with additional fields for standard tenant registration
ALTER TABLE tenants 
ADD COLUMN current_users INTEGER DEFAULT 0,
ADD COLUMN trial_ends_at TIMESTAMP NULL,
ADD COLUMN subscription_ends_at TIMESTAMP NULL,
ADD COLUMN company_name VARCHAR(255),
ADD COLUMN company_address VARCHAR(500),
ADD COLUMN company_phone VARCHAR(50),
ADD COLUMN company_email VARCHAR(255),
ADD COLUMN industry VARCHAR(100),
ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC',
ADD COLUMN locale VARCHAR(10) DEFAULT 'en',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update subscription_plan to use ENUM type
ALTER TABLE tenants 
ALTER COLUMN subscription_plan TYPE VARCHAR(50);

-- Add constraint for subscription plan values
ALTER TABLE tenants 
ADD CONSTRAINT chk_subscription_plan 
CHECK (subscription_plan IN ('FREE', 'STARTER', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM'));

-- Create indexes for better performance
CREATE INDEX idx_tenants_subscription_plan ON tenants(subscription_plan);
CREATE INDEX idx_tenants_trial_ends_at ON tenants(trial_ends_at);
CREATE INDEX idx_tenants_is_active ON tenants(is_active);

-- Update existing default tenant
UPDATE tenants 
SET 
    company_name = 'Default Company',
    current_users = 1,
    trial_ends_at = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 14 DAY),
    timezone = 'UTC',
    locale = 'en'
WHERE tenant_id = 1;