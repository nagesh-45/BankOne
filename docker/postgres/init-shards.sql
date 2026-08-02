-- Shard lab databases (same Postgres instance as bankone).
-- Mounted at /docker-entrypoint-initdb.d — runs ONLY on first init of an empty volume.
-- If bankone_pg already exists, create DBs once manually:
--   docker exec -i <db-container> psql -U bankone_user -d postgres <<'SQL'
--   CREATE DATABASE bankone_s0;
--   CREATE DATABASE bankone_s1;
--   GRANT ALL PRIVILEGES ON DATABASE bankone_s0 TO bankone_user;
--   GRANT ALL PRIVILEGES ON DATABASE bankone_s1 TO bankone_user;
--   SQL

CREATE DATABASE bankone_s0;
CREATE DATABASE bankone_s1;

GRANT ALL PRIVILEGES ON DATABASE bankone_s0 TO bankone_user;
GRANT ALL PRIVILEGES ON DATABASE bankone_s1 TO bankone_user;
