CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID REFERENCES users(id),
                                token VARCHAR(512) UNIQUE NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP DEFAULT NOW()
);