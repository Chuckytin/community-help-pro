-- Habilita PostGIS en la base de datos.
CREATE EXTENSION IF NOT EXISTS postgis;
-- Crea un índice espacial GIST sobre la columna 'location' de la tabla 'users' para optimizar las búsquedas por proximidad.
CREATE INDEX IF NOT EXISTS idx_users_location_gist ON users USING GIST(location);