CREATE TABLE IF NOT EXISTS user_invitations (
    invitation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    invited_by_user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invitations_tenant ON user_invitations (tenant_id);
CREATE INDEX idx_invitations_token ON user_invitations (token);

