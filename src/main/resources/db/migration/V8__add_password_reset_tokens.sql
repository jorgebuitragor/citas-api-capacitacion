-- HU-004 (DEC-008): tokens de recuperación de contraseña de un solo uso.
-- Mismo patrón que refresh_tokens: solo se persiste el hash, nunca el valor en texto plano.
CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255),
    expires_at DATETIME(6),
    consumed_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
