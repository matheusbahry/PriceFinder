-- Extensão para geração automática de UUIDs no PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE role_name AS ENUM (
    'ADMIN',
    'OPERADOR',
    'USER'
);