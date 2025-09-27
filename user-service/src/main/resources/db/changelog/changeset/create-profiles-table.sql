CREATE TABLE user_profiles (
                          user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                          nickname VARCHAR(30),
                          avatar_url VARCHAR(512),
                          status VARCHAR(20) DEFAULT 'online' CHECK (status IN ('online', 'offline', 'idle', 'dnd')),
                          last_online TIMESTAMP WITH TIME ZONE,
                          bio TEXT
);