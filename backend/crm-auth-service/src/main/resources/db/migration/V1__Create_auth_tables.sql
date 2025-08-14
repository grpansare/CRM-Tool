-- Create tenants table
CREATE TABLE tenants (
    tenant_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    subscription_plan VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    max_users INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER
);

-- Create users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    manager_id INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    FOREIGN KEY (manager_id) REFERENCES users(user_id)
);

-- Create user_permissions table
CREATE TABLE user_permissions (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    scope VARCHAR(50) NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_manager_id ON users(manager_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_user_permissions_tenant_user ON user_permissions(tenant_id, user_id);
CREATE INDEX idx_user_permissions_resource_action ON user_permissions(resource, action);

-- Insert default tenant for testing
INSERT INTO tenants (tenant_id, tenant_name, domain, subscription_plan, is_active, max_users) VALUES 
(1, 'Default Tenant', 'default.crmplatform.com', 'ENTERPRISE', true, 100);

-- Insert default super admin user
-- Password: admin123 (BCrypt hash)
INSERT INTO users (user_id, tenant_id, username, email, password_hash, first_name, last_name, role, is_active) VALUES 
(1, 1, 'admin', 'admin@crmplatform.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'System', 'Administrator', 'SUPER_ADMIN', true);

-- Insert default permissions for super admin
INSERT INTO user_permissions (tenant_id, user_id, resource, action, scope) VALUES 
(1, 1, 'CONTACTS', 'READ', 'ALL'),
(1, 1, 'CONTACTS', 'WRITE', 'ALL'),
(1, 1, 'DEALS', 'READ', 'ALL'),
(1, 1, 'DEALS', 'WRITE', 'ALL'),
(1, 1, 'ACCOUNTS', 'READ', 'ALL'),
(1, 1, 'ACCOUNTS', 'WRITE', 'ALL'),
(1, 1, 'SETTINGS', 'READ', 'ALL'),
(1, 1, 'SETTINGS', 'WRITE', 'ALL'); 