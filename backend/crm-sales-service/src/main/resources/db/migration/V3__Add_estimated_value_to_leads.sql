-- Add estimated_value column to leads table
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS estimated_value DECIMAL(15, 2);

-- Add comment to describe the column
COMMENT ON COLUMN leads.estimated_value IS 'The estimated monetary value of the lead/opportunity in the default currency';

-- Update existing leads with NULL estimated_value to 0.00 if needed
-- UPDATE leads SET estimated_value = 0.00 WHERE estimated_value IS NULL;
-- Note: Uncomment the above line if you want to set a default value for existing records
