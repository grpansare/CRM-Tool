-- Add disposition tracking fields to leads table
ALTER TABLE leads 
ADD COLUMN current_disposition VARCHAR(50),
ADD COLUMN disposition_notes TEXT,
ADD COLUMN last_contact_date TIMESTAMP,
ADD COLUMN next_follow_up_date TIMESTAMP,
ADD COLUMN disposition_updated_at TIMESTAMP,
ADD COLUMN disposition_updated_by BIGINT;

-- Add index for better query performance
CREATE INDEX idx_leads_disposition ON leads(current_disposition);
CREATE INDEX idx_leads_next_follow_up ON leads(next_follow_up_date);
CREATE INDEX idx_leads_last_contact ON leads(last_contact_date);

-- Add foreign key constraint for disposition_updated_by (references user)
-- Note: Uncomment if you have a users table in this database
-- ALTER TABLE leads ADD CONSTRAINT fk_leads_disposition_updated_by 
-- FOREIGN KEY (disposition_updated_by) REFERENCES users(user_id);
