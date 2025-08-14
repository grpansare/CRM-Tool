-- Create accounts table
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    website VARCHAR(255),
    industry VARCHAR(100),
    owner_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_account_name (tenant_id, account_name)
);

-- Create contacts table
CREATE TABLE contacts (
    contact_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    primary_email VARCHAR(255),
    phone_number VARCHAR(50),
    job_title VARCHAR(100),
    owner_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_primary_email (tenant_id, primary_email)
);

-- Create account_contacts join table
CREATE TABLE account_contacts (
    account_id INT NOT NULL,
    contact_id INT NOT NULL,
    tenant_id INT NOT NULL,
    PRIMARY KEY (tenant_id, account_id, contact_id),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(contact_id)
);

-- Create custom_fields_data table
CREATE TABLE custom_fields_data (
    data_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    entity_id INT NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    CHECK (entity_type IN ('contact', 'account', 'deal'))
);

-- Create indexes for better performance
CREATE INDEX idx_accounts_tenant_id ON accounts(tenant_id);
CREATE INDEX idx_accounts_owner_user_id ON accounts(owner_user_id);
CREATE INDEX idx_contacts_tenant_id ON contacts(tenant_id);
CREATE INDEX idx_contacts_owner_user_id ON contacts(owner_user_id);
CREATE INDEX idx_contacts_primary_email ON contacts(primary_email);
CREATE INDEX idx_account_contacts_tenant_id ON account_contacts(tenant_id);
CREATE INDEX idx_custom_fields_tenant_entity ON custom_fields_data(tenant_id, entity_id, entity_type); 