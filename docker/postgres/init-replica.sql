-- Read replica DB (same Postgres instance as bankone).
-- Mounted via docker-compose init — runs ONLY on first empty volume.
-- Manual (existing volume):
--   CREATE DATABASE bankone_read OWNER bankone_user;
--   GRANT ALL PRIVILEGES ON DATABASE bankone_read TO bankone_user;

CREATE DATABASE bankone_read;
GRANT ALL PRIVILEGES ON DATABASE bankone_read TO bankone_user;
