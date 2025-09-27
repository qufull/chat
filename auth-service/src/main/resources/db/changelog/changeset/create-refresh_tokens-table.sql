CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID REFERENCES users(id),
                                access_token VARCHAR(512) UNIQUE NOT NULL,
                                refresh_token VARCHAR(512) UNIQUE NOT NULL,
                                is_logged_out BOOLEAN NOT NULL DEFAULT FALSE,
                                UNIQUE(access_token),
                                UNIQUE(refresh_token)

);