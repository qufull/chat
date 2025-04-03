CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,  -- bcrypt/scrypt
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT NOW(),
                       updated_at TIMESTAMP DEFAULT NOW()
);