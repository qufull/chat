CREATE TABLE user_profiles (
                          id VARCHAR(128) UNIQUE NOT NULL,
                          nickname VARCHAR(30),
                          avatar_url VARCHAR(512),
                          status VARCHAR(20) DEFAULT 'ONLINE' CHECK (status IN ('ONLINE', 'OFFLINE')),
                          last_seen TIMESTAMP WITH TIME ZONE,
                          about TEXT
);