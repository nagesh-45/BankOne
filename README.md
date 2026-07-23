# BankOne

Monorepo:

| Path | Contents |
|------|----------|
| `BankOne-BackEnd/` | Spring Boot 4 / Java 21 (Liberty locally, Docker/cloud via `-Pdocker`) |
| `BankOne-Frontend/` | Angular SPA |
| `docs/` | Product docs (Word/PDF) + [DEPLOY.md](docs/DEPLOY.md) |
| `scripts/` | Liberty redeploy, docs regenerate |
| `docker-compose.yml` | Local Postgres + API + UI |

## Local development

```bash
# Backend (Liberty)
./scripts/redeploy-liberty.sh

# Frontend
cd BankOne-Frontend && npm start
```

API URL for local UI: `BankOne-Frontend/src/environments/environment.ts`

## Go live (GitHub → hosting)

See **[docs/DEPLOY.md](docs/DEPLOY.md)**.

Repo: https://github.com/nagesh-45/BankOne

```bash
git add .
git commit -m "Prepare deployment (Docker, CI, env config)"
git push origin main
```

## Docker (full stack on your machine)

```bash
cp .env.example .env
docker compose up --build
# UI http://localhost:8081  API http://localhost:8080
```
