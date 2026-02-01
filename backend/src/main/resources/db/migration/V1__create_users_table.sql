-- V1: Create Users Table
-- Store user authentication and profile information

CREATE TABLE IF NOT EXISTS users
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username           VARCHAR(50)  NOT NULL UNIQUE,
    email              VARCHAR(255) NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled            BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_role CHECK ( role IN ('USER', 'ADMIN') ),
    CONSTRAINT chk_username_length CHECK ( LENGTH(username) >= 3 AND LENGTH(username) <= 50 ),
    CONSTRAINT chk_email_format CHECK ( email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


INSERT INTO users (username, email, password_hash, role)
VALUES ('admin',
        'admin@eventhub.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Ak5gO6QWRE8i',
        'ADMIN')
ON CONFLICT (username) DO NOTHING;

COMMENT ON TABLE users IS 'User authentication and profile information';
COMMENT ON COLUMN users.id IS 'Primary key (UUID V4)';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'User email address (unique)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password (cost factor 12)';
COMMENT ON COLUMN users.role IS 'User role (USER or ADMIN)';
COMMENT ON COLUMN users.enabled IS 'Account enabled status';
COMMENT ON COLUMN users.account_non_locked IS 'Account lock status';


