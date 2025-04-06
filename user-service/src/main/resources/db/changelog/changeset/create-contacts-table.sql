CREATE TABLE contacts (
                          user_id UUID REFERENCES users(id),
                          contact_id UUID REFERENCES users(id),
                          status VARCHAR(20) NOT NULL,  -- pending/accepted/blocked
                          created_at TIMESTAMP DEFAULT NOW(),
                          CONSTRAINT uk_user_contact UNIQUE (user_id, contact_id)
);