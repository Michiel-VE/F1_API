-- Flyway migration script for PostgreSQL to create the users table,
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       provider VARCHAR(50),

                       provider_id VARCHAR(255),

                       email VARCHAR(255) UNIQUE NOT NULL,

                       password VARCHAR(255),

                       name VARCHAR(255),

                       role VARCHAR(50) NOT NULL DEFAULT 'USER',

                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT check_authentication_type CHECK (
                               (provider IS NOT NULL AND provider_id IS NOT NULL AND password IS NULL) OR
                               (provider IS NULL AND provider_id IS NULL AND password IS NOT NULL)
                           ),

                       UNIQUE (provider, provider_id)
);

CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_users_name ON users (name);