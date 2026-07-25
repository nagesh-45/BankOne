# Development Guide

## Prerequisites

- JDK **21**
- Maven 3.9+
- Node.js + Angular CLI (frontend)
- PostgreSQL with DB `bankone` and user `bankone_user`
- Optional: Open Liberty under `~/tools/wlp` (server name `bankone`)

## Backend

    cd "BankOne/BankOne-BackEnd"
    # Embedded
    mvn spring-boot:run
    # Liberty (recommended) — rebuilds WAR, always enables JDWP :7777
    ./scripts/redeploy-liberty.sh
    # Attach IntelliJ: Remote JVM Debug → localhost:7777
    # Disable debug: LIBERTY_DEBUG=0 ./scripts/redeploy-liberty.sh

### Local Kafka + Mailpit (notifications)

    cd "BankOne"
    docker compose up -d kafka mailpit
    # Mailpit UI: http://localhost:8025
    # Kafka: localhost:9092

### Load-test data (optional)

    psql -h localhost -U bankone_user -d bankone \
      -f scripts/seed-loadtest-10k.sql
    # Search UI for LoadTest; filter accounts by branch 9999

Key config: `BankOne-BackEnd/src/main/resources/application.properties`

  -----------------------------------------------------------------------
  Property                                 Purpose
  ---------------------------------------- ------------------------------
  `spring.datasource.*`                    PostgreSQL connection

  `spring.jpa.hibernate.ddl-auto=update`   Schema sync

  `spring.sql.init.mode=always`            Runs `schema.sql`

  `server.port=8080`                       Embedded only

  `jwt.secret` / `jwt.expiration`          JWT signing

  `app.kafka.*` / `KAFKA_*`                Kafka notifications

  `app.mail.*` / `MAIL_*`                  SMTP or SendGrid transport
  -----------------------------------------------------------------------

After Liberty redeploy, smoke login expects `admin` / `Admin@123`.

## Frontend

    cd "BankOne/BankOne-Frontend"
    npm install   # first time
    ng serve

Set API host in `src/app/core/config/api-config.ts` (currently points at
Liberty / LAN host on port **9080**).

## Coding conventions

### Backend

- Package by domain:
  `com.bankone.{module}.{controller|service|repository|entity|dto|...}`
- Prefer service interface + `*Impl`
- Search via JPA `Specification` classes
- Map API responses through DTOs for accounts/policies; customers
  currently return entity (known smell)
- Security roles: `ROLE_` prefix handled by Spring; matchers use
  `ADMIN`, `EMPLOYEE`, `MANAGER`

### Frontend

- Feature folders under `src/app/features/`
- Shared HTTP in `src/app/core/services/`
- Models in `src/app/core/models/`
- Bearer token via `auth.interceptor.ts`
- Dialogs as standalone components (e.g. `opening-deposit-dialog/`)
- Shared list footer: `app-list-pagination` (First/Prev/Next/Last +
  Go to page; 1-based UI → 0-based API `page`)

## Teach-while-building

Project rule (`.cursor/rules/teach-while-building.mdc`): unless the user
says to apply code, explain snippets for IntelliJ paste. Documentation
sync still applies when code lands.

## Documentation sync checklist (every change)

1.  Update module doc under `docs/MODULES/`
2.  Update API / schema / sequences / call flow if surfaces changed
3.  Append `docs/CHANGELOG.md`
4.  Refresh Mermaid diagrams when structure changes

## Common pitfalls

  ------------------------------------------------------------
  Symptom               Likely cause
  --------------------- --------------------------------------
  405 on new GET        Stale Liberty WAR --- redeploy
  endpoint              

  Opening CURRENT fails Policy requires min opening (seed
  on deposit            CURRENT/INR = 5000)

  CORS errors from      Firewall / AP isolation; CORS already
  phone/LAN             allows `192.168.0.4`

  Roles missing after   Roles not in JWT --- check
  login                 `user_roles` and filter reload

  Email shows           Do not concatenate `Customer`
  `Customer@hash`       entity --- use `customerLabel`

  Render mail timeout   Use `MAIL_TRANSPORT=sendgrid`
  on SMTP               (HTTPS); outbound SMTP blocked

  Render Docker CACHED  Manual Deploy → Clear build
  old WAR               cache & deploy
  ------------------------------------------------------------

## Suggested next build slices

1.  Notify both parties on transfer; Kafka outbox
2.  Real customer portal / statement PDF
3.  Deferred hardening list (security / validation)
4.  Status transition rules / maker--checker
