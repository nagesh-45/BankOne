# Development Guide

## Prerequisites

- JDK **21**
- Maven 3.9+
- Node.js + Angular CLI (frontend)
- PostgreSQL with DB `bankone` and user `bankone_user`
- Optional: Open Liberty under `~/tools/wlp` (server name `bankone`)

## Backend

    cd "BankOne/BankOne"
    # Embedded
    mvn spring-boot:run
    # Liberty (recommended for this project)
    ./scripts/redeploy-liberty.sh

Key config: `src/main/resources/application.properties`

  -----------------------------------------------------------------------
  Property                                 Purpose
  ---------------------------------------- ------------------------------
  `spring.datasource.*`                    PostgreSQL connection

  `spring.jpa.hibernate.ddl-auto=update`   Schema sync

  `spring.sql.init.mode=always`            Runs `schema.sql`

  `server.port=8080`                       Embedded only

  `jwt.secret` / `jwt.expiration`          JWT signing
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
  ------------------------------------------------------------

## Suggested next build slices

1.  Transaction ledger foundation
2.  Account detail + debit/credit history
3.  Withdraw
4.  Account list filters / polish
5.  Status transition rules
6.  Maker--checker
