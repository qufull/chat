CREATE TABLE user_profiles (
                          id VARCHAR(128) UNIQUE NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          nickname VARCHAR(50) NOT NULL,
                          avatar_url VARCHAR(512),
                          bio VARCHAR(500),

                          status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
                          last_seen_at TIMESTAMP WITH TIME ZONE,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          version BIGINT DEFAULT 0,

                          CONSTRAINT chk_status CHECK (status IN ('ONLINE', 'AWAY', 'DO_NOT_DISTURB', 'OFFLINE')),
                          CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_user_profiles_nickname ON user_profiles(nickname);
CREATE INDEX idx_user_profiles_email ON user_profiles(email);
CREATE INDEX idx_user_profiles_status ON user_profiles(status) WHERE is_active = TRUE;
CREATE INDEX idx_user_profiles_last_seen ON user_profiles(last_seen_at DESC) WHERE is_active = TRUE;

CREATE INDEX idx_user_profiles_nickname_search ON user_profiles USING gin(to_tsvector('simple', nickname));