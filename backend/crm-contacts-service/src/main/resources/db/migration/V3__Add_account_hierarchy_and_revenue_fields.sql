-- Add account hierarchy and revenue tracking fields to accounts table

ALTER TABLE accounts 
ADD COLUMN parent_account_id BIGINT,
ADD COLUMN account_type VARCHAR(50),
ADD COLUMN annual_revenue DECIMAL(15,2),
ADD COLUMN employee_count INTEGER,
ADD COLUMN description TEXT;

-- Add foreign key constraint for parent account relationship
ALTER TABLE accounts 
ADD CONSTRAINT fk_accounts_parent 
FOREIGN KEY (parent_account_id) REFERENCES accounts(account_id);

-- Add indexes for better query performance
CREATE INDEX idx_accounts_parent_account_id ON accounts(parent_account_id);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_accounts_annual_revenue ON accounts(annual_revenue);

-- Add comments for documentation
COMMENT ON COLUMN accounts.parent_account_id IS 'Reference to parent account for hierarchy support';
COMMENT ON COLUMN accounts.account_type IS 'Type of account: PARENT, SUBSIDIARY, DIVISION, BRANCH, PARTNER, VENDOR, CUSTOMER, PROSPECT';
COMMENT ON COLUMN accounts.annual_revenue IS 'Annual revenue of the account in decimal format';
COMMENT ON COLUMN accounts.employee_count IS 'Number of employees in the account';
COMMENT ON COLUMN accounts.description IS 'Detailed description of the account';
