-- This migration creates the user_profile table.

CREATE TABLE f1_api."user"
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE f1_api.user_profile
(
    id                  UUID PRIMARY KEY,

    user_id             UUID                     NOT NULL,

    first_name          VARCHAR(255),
    last_name           VARCHAR(255),
    profile_picture_url VARCHAR(255),

    created_at          timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at          timestamp with time zone NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES f1_api."user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_profile_user_id ON f1_api.user_profile (user_id);
