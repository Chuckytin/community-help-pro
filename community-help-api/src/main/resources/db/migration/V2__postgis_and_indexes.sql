-- Habilita PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Índice espacial para búsquedas por proximidad
CREATE INDEX IF NOT EXISTS idx_users_location_gist ON users USING GIST(location);

CREATE INDEX IF NOT EXISTS idx_donations_location_gist ON donations USING GIST(location);

CREATE INDEX IF NOT EXISTS idx_help_requests_location_gist ON help_requests USING GIST(location);

-- Índices adicionales de consulta frecuente

-- proposals: el sistema consulta muy frecuentemente por (volunteer + status)
-- y por (target_entity + status), combinaciones compuestas que son más eficientes
CREATE INDEX IF NOT EXISTS idx_proposal_volunteer_status ON proposals(volunteer_id, status);

CREATE INDEX IF NOT EXISTS idx_proposal_target_status ON proposals(target_entity_id, status);

-- proposals: el retry service filtra por (type + status + created_at)
CREATE INDEX IF NOT EXISTS idx_proposal_type_status_created ON proposals(type, status, created_at);

-- pending_notifications: el digest filtra por sent=false frecuentemente
CREATE INDEX IF NOT EXISTS idx_pending_notifications_sent ON pending_notifications(sent);

-- pending_notifications: deduplicación por (volunteer_id + entity_id + sent)
CREATE INDEX IF NOT EXISTS idx_pending_notifications_dedup ON pending_notifications(volunteer_id, entity_id, sent);

-- help_requests y donations: el matching filtra por active=true
CREATE INDEX IF NOT EXISTS idx_help_requests_active_status ON help_requests(active, status);

CREATE INDEX IF NOT EXISTS idx_donations_active_status ON donations(active, status);

-- proposal_matching_state: consultas por last_retry_at para el retry scheduler
CREATE INDEX IF NOT EXISTS idx_matching_state_last_retry ON proposal_matching_state(last_retry_at);

-- otp_codes: limpieza y validación por email + type + used
CREATE INDEX IF NOT EXISTS idx_otp_email_type_used ON otp_codes(email, type, used);