CREATE TABLE IF NOT EXISTS user_tokens (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL,
    expires_at timestamp NOT NULL,
    created_at timestamp NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_tokens_token_hash ON user_tokens (token_hash);

CREATE INDEX IF NOT EXISTS ix_user_tokens_user_id ON user_tokens (user_id);

UPDATE users
SET cookie_hash = NULL, cookie_expires_at = NULL
WHERE cookie_hash IS NOT NULL OR cookie_expires_at IS NOT NULL;
