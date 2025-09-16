-- Add employee_count, annual_revenue, and industry columns to leads table
ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS employee_count INT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS annual_revenue BIGINT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS industry VARCHAR(100) DEFAULT NULL;
