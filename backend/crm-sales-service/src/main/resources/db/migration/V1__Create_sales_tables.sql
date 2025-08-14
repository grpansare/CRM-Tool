-- Create sales_pipelines table
CREATE TABLE sales_pipelines (
    pipeline_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    pipeline_name VARCHAR(100) NOT NULL
);

-- Create pipeline_stages table
CREATE TABLE pipeline_stages (
    stage_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    pipeline_id INT NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    stage_order INT NOT NULL,
    stage_type VARCHAR(10),
    win_probability DECIMAL(5, 2),
    FOREIGN KEY (pipeline_id) REFERENCES sales_pipelines(pipeline_id),
    CHECK (stage_type IN ('OPEN', 'WON', 'LOST'))
);

-- Create deals table
CREATE TABLE deals (
    deal_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    deal_name VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    expected_close_date DATE,
    stage_id INT NOT NULL,
    contact_id INT NOT NULL,
    account_id INT NOT NULL,
    owner_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stage_id) REFERENCES pipeline_stages(stage_id)
);

-- Create deal_stage_history table
CREATE TABLE deal_stage_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    deal_id INT NOT NULL,
    from_stage_id INT,
    to_stage_id INT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_in_previous_stage_days INT,
    FOREIGN KEY (deal_id) REFERENCES deals(deal_id),
    FOREIGN KEY (from_stage_id) REFERENCES pipeline_stages(stage_id),
    FOREIGN KEY (to_stage_id) REFERENCES pipeline_stages(stage_id)
);

-- Create indexes for better performance
CREATE INDEX idx_sales_pipelines_tenant_id ON sales_pipelines(tenant_id);
CREATE INDEX idx_pipeline_stages_tenant_id ON pipeline_stages(tenant_id);
CREATE INDEX idx_pipeline_stages_pipeline_id ON pipeline_stages(pipeline_id);
CREATE INDEX idx_pipeline_stages_stage_order ON pipeline_stages(stage_order);
CREATE INDEX idx_deals_tenant_id ON deals(tenant_id);
CREATE INDEX idx_deals_owner_user_id ON deals(owner_user_id);
CREATE INDEX idx_deals_stage_id ON deals(stage_id);
CREATE INDEX idx_deals_contact_id ON deals(contact_id);
CREATE INDEX idx_deals_account_id ON deals(account_id);
CREATE INDEX idx_deal_stage_history_tenant_id ON deal_stage_history(tenant_id);
CREATE INDEX idx_deal_stage_history_deal_id ON deal_stage_history(deal_id);
CREATE INDEX idx_deal_stage_history_changed_at ON deal_stage_history(changed_at);

-- Insert default pipeline and stages for testing
INSERT INTO sales_pipelines (tenant_id, pipeline_name) VALUES 
(1, 'Standard Sales Pipeline');

INSERT INTO pipeline_stages (tenant_id, pipeline_id, stage_name, stage_order, stage_type, win_probability) VALUES 
(1, 1, 'Lead', 1, 'OPEN', 10.00),
(1, 1, 'Qualified', 2, 'OPEN', 25.00),
(1, 1, 'Proposal', 3, 'OPEN', 50.00),
(1, 1, 'Negotiation', 4, 'OPEN', 75.00),
(1, 1, 'Won', 5, 'WON', 100.00),
(1, 1, 'Lost', 6, 'LOST', 0.00); 