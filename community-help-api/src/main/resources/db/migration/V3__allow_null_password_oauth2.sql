-- Permite contraseña nula para usuarios que se autentican con OAuth2 (Google, etc.)
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;