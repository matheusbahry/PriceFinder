CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE mercado_livre_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);