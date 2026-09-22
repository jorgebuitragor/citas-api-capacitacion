-- Preserve the UUID-era authentication tables. They remain available for
-- audit/migration purposes but are not read by the current persistence adapter.
SET FOREIGN_KEY_CHECKS = 0;
RENAME TABLE roles TO legacy_roles,
             users TO legacy_users,
             user_roles TO legacy_user_roles,
             refresh_sessions TO legacy_refresh_sessions;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE roles (
    id SMALLINT NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_code UNIQUE (code),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    document_type VARCHAR(255),
    document_number VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    password_hash VARCHAR(255),
    active BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_document UNIQUE (document_type, document_number)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id SMALLINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_active_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_active_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255),
    expires_at DATETIME(6),
    revoked_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

INSERT INTO roles (id, code, name) VALUES
    (1, 'USER', 'USER'),
    (2, 'ADMIN', 'ADMIN'),
    (3, 'PROFESSIONAL', 'PROFESSIONAL');
