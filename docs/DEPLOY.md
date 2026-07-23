# Deploy BankOne from GitHub

Your code already has a remote: **https://github.com/nagesh-45/BankOne**

GitHub stores the code. Hosting (Render / Railway / Fly / etc.) runs the app.

## 0. Push current work

```bash
cd "/Users/nageshkumargandla/Java Projects/BankOne/BankOne"
cp .env.example .env   # edit secrets locally; never commit .env
git add .
git status
git commit -m "Add Docker, env config, and CI for deployment"
git push origin main
```

CI runs on every push (`.github/workflows/ci.yml`): Maven package + Angular production build.

---

## 1. Easiest path: Render (or Railway)

### A. PostgreSQL

1. Create a **PostgreSQL** service on Render/Railway.
2. Copy the JDBC URL, user, password.

### B. Backend API

1. New **Web Service** from GitHub repo `nagesh-45/BankOne`.
2. Root / Dockerfile path: `BankOne-BackEnd`
3. Env vars:

| Variable | Example |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...` (from host) |
| `SPRING_DATASOURCE_USERNAME` | from host |
| `SPRING_DATASOURCE_PASSWORD` | from host |
| `JWT_SECRET` | long random string (32+ chars) |
| `PORT` | `8080` (or host’s port) |
| `APP_CORS_ORIGINS` | `https://your-frontend.onrender.com` |

4. Deploy → note public API URL, e.g. `https://bankone-api.onrender.com`

### C. Frontend

**Option 1 — Netlify / Cloudflare Pages / Vercel (static)**

1. Set `BankOne-Frontend/src/environments/environment.prod.ts` → `apiBaseUrl: 'https://bankone-api.onrender.com'`
2. Commit + push.
3. Connect GitHub; build command:

```bash
cd BankOne-Frontend && npm ci && npm run build -- --configuration=production
```

4. Publish directory: `BankOne-Frontend/dist/BankOne-Frontend/browser`

**Option 2 — Docker web service**

Use `BankOne-Frontend/Dockerfile` with build-arg:

`API_BASE_URL=https://bankone-api.onrender.com`

---

## 2. Local “live-like” stack (Docker Compose)

Requires Docker Desktop.

```bash
cp .env.example .env
# set JWT_SECRET and passwords
docker compose up --build
```

| Service | URL |
|---------|-----|
| UI | http://localhost:8081 |
| API | http://localhost:8080 |
| Postgres | localhost:5432 |

Login: `admin` / `Admin@123` (seeded by your app initializers).

---

## 3. Checklist before public internet

- [ ] Strong `JWT_SECRET` and DB password (not defaults)
- [ ] `APP_CORS_ORIGINS` = exact frontend HTTPS origin
- [ ] `environment.prod.ts` API URL correct
- [ ] Change default admin password after first login
- [ ] Never commit `.env` or real secrets
- [ ] HTTPS only in production

---

## 4. Liberty (your current local path)

Local Liberty is unchanged:

```bash
./scripts/redeploy-liberty.sh
```

Cloud deploys use **embedded Tomcat** via Maven profile `-Pdocker` (see `BankOne-BackEnd/Dockerfile`).

---

## 5. What we added in the repo

| File | Purpose |
|------|---------|
| `.gitignore` | Ignore `node_modules`, `target`, `.env`, secrets |
| `.env.example` | Template for compose / cloud env |
| `docker-compose.yml` | Local full stack |
| `BankOne-BackEnd/Dockerfile` | API image |
| `BankOne-Frontend/Dockerfile` | UI image (nginx) |
| `.github/workflows/ci.yml` | Build on push/PR |
| Env-based `application.properties` | Cloud-ready config |
| `environment.ts` / `environment.prod.ts` | Dev vs prod API URL |
