CREATE TABLE user_roles (
                            user_id UUID REFERENCES users(id),
                            role_id INTEGER REFERENCES roles(id),
                            PRIMARY KEY (user_id, role_id)
);
