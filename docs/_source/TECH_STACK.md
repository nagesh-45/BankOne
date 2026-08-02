# Technology Stack

Living inventory of technologies used in BankOne. **Update this document
whenever dependencies, runtimes, or tooling change** (add, upgrade,
remove, or replace). Source of truth for versions: `BankOne-BackEnd/pom.xml`
and `BankOne-Frontend/package.json`.

Last reviewed: 2026-08-02

## At a glance

  -----------------------------------------------------------------------
  Layer                Primary stack
  -------------------- --------------------------------------------------
  Backend              Java 21 · Spring Boot 4.1 · Maven · WAR
  Frontend             Angular 22 · TypeScript 6 · npm · SCSS
  Database             PostgreSQL (`bankone` write; optional
                       `bankone_read` replica lab)
  Cache / rate limit   Redis (local Docker; entity cache + Bucket4j)
  Messaging            Apache Kafka (local Docker / Aiven on Render)
  Email                Mailpit (local SMTP) · Twilio SendGrid HTTPS (Render)
  Auth                 Spring Security · JWT (JJWT)
  Reporting            Chart.js (UI) · OpenPDF (server PDF)
  Audit                `audit_event` activity trail + backfill
  Deploy (primary)     Open Liberty (`:9080` / `:9443`, JDWP `:7777`)
  Deploy (cloud)       Render (Docker `-Pdocker`) · Netlify (Angular)
  Docs tooling         Pandoc · Google Chrome (PDF) · python-docx
  Learning roadmap     [TECH_LEARNING_PLAN.md](./TECH_LEARNING_PLAN.md)
  -----------------------------------------------------------------------

## Backend (`BankOne-BackEnd/`)

### Language & build

  -----------------------------------------------------------------------
  Technology           Version / notes                   Where defined
  -------------------- --------------------------------- ----------------
  Java                 21                                `pom.xml`
                                                         `java.version`
  Maven                3.9+ (wrapper present)            `.mvn/`
  Spring Boot          4.1.0 (parent POM)                `pom.xml`
  Packaging            WAR (`bankone-…-SNAPSHOT.war`)    `pom.xml`
                                                         `<packaging>`
  -----------------------------------------------------------------------

### Spring & persistence

  -----------------------------------------------------------------------
  Technology           Purpose
  -------------------- --------------------------------------------------
  spring-boot-starter- Web / REST (`@RestController`)
  webmvc

  spring-boot-starter- JPA repositories, entities,
  data-jpa             specifications

  Hibernate (via       ORM; `ddl-auto=update`; SQL
  Spring Data JPA)     logging in `application.properties`

  spring-boot-starter- Bean Validation (`@Valid`,
  validation           constraints)

  spring-boot-starter- Actuator endpoints
  actuator

  spring-boot-devtools Hot reload (runtime, optional)
  -----------------------------------------------------------------------

### Security & tokens

  -----------------------------------------------------------------------
  Technology           Version / notes
  -------------------- --------------------------------------------------
  spring-boot-starter- Stateless session; filters;
  security             method/URL authorization

  JJWT (jjwt-api,      0.12.7 — HMAC JWT create/parse
  jjwt-impl,
  jjwt-jackson)
  -----------------------------------------------------------------------

### Database & JDBC

  -----------------------------------------------------------------------
  Technology           Notes
  -------------------- --------------------------------------------------
  PostgreSQL           Primary DB `bankone`; optional read lab DB
                       `bankone_read` (`app.replica.*`)

  PostgreSQL JDBC      `org.postgresql:postgresql` (runtime)
  driver

  schema.sql /         SQL init (`spring.sql.init.mode=always`)
  data.sql
  -----------------------------------------------------------------------

### Redis — cache-aside & rate limiting (local)

  -----------------------------------------------------------------------
  Technology           Purpose
  -------------------- --------------------------------------------------
  Redis                Docker Compose service typically `:6379`
                       (`REDIS_HOST` / `REDIS_PORT`)

  spring-boot-starter- Lettuce client; shared host for rate limit +
  data-redis           entity cache

  spring-boot-starter- Spring `@Cacheable` / `@CacheEvict`
  cache

  Bucket4j + Lettuce   Login / API rate-limit buckets when
                       `app.rate-limit.redis-enabled=true`

  Entity cache         `com.bankone.cache` — cache-aside for major
                       reads; `app.cache.redis-enabled` (prod off);
                       JDK value serialization; keys
                       `bankone:<region>::…`

  Docs                 [MODULES/Caching.md](./MODULES/Caching.md)
  -----------------------------------------------------------------------

### Messaging & email

  -----------------------------------------------------------------------
  Technology           Purpose
  -------------------- --------------------------------------------------
  spring-boot-starter- Kafka produce/consume
  kafka                (`com.bankone.notification`)

  spring-boot-starter- SMTP path (Mailpit locally)
  mail

  Apache Kafka 3.9     Local: `docker compose` service
                       `kafka` (`apache/kafka:3.9.0` :9092)

  Aiven Kafka          Render managed broker (SASL_SSL +
                       SCRAM + CA PEM)

  Mailpit              Local fake inbox SMTP `:1025` /
                       UI `:8025`

  Twilio SendGrid      Render email via HTTPS API when
                       `MAIL_TRANSPORT=sendgrid`
  -----------------------------------------------------------------------

### Code generation & container

  -----------------------------------------------------------------------
  Technology           Notes
  -------------------- --------------------------------------------------
  Lombok               Optional; annotation processing in
                       `maven-compiler-plugin`

  spring-boot-starter- Embedded Tomcat for
  tomcat               `spring-boot:run` / local WAR
                       (`provided` for Liberty deploy)
  -----------------------------------------------------------------------

### Test dependencies

  -----------------------------------------------------------------------
  Technology
  --------------------------------------------------------------------
  spring-boot-starter-data-jpa-test
  spring-boot-starter-security-test
  spring-boot-starter-validation-test
  spring-boot-starter-webmvc-test
  -----------------------------------------------------------------------

## Frontend (`BankOne-Frontend/`)

### Core framework

  -----------------------------------------------------------------------
  Technology           Version / notes                   Where defined
  -------------------- --------------------------------- ----------------
  Angular              ^22.0.x (core, router, forms,     `package.json`
                       common, animations, compiler,
                       platform-browser)
  TypeScript           ~6.0.2                            `package.json`
  RxJS                 ~7.8.0                            `package.json`
  tslib                ^2.3.0                            `package.json`
  npm                  11.16.0 (`packageManager`)        `package.json`
  SCSS                 Component / global styles         `angular.json`
  -----------------------------------------------------------------------

### UI libraries

  -----------------------------------------------------------------------
  Technology           Version / notes
  -------------------- --------------------------------------------------
  Angular Material     ^22.0.5 — buttons, dialogs, forms,
                       icons, cards, etc.

  Angular CDK          ^22.0.5 — Material peer dependency
  -----------------------------------------------------------------------

### Build & quality tooling

  -----------------------------------------------------------------------
  Technology           Version / notes
  -------------------- --------------------------------------------------
  Angular CLI          ^22.0.7
  @angular/build       ^22.0.7 — application builder
  Prettier             ^3.8.1
  Vitest               ^4.0.8 — unit tests (`ng test`)
  jsdom                ^28.0.0 — test DOM
  -----------------------------------------------------------------------

### Runtime patterns (no extra packages)

  -----------------------------------------------------------------------
  Concern              Approach
  -------------------- --------------------------------------------------
  HTTP                 Angular `HttpClient`
  Auth                 JWT in browser storage; Bearer
                       interceptor; `authGuard` + roles
  Layout               App shell + sidebar under `core/layout`
  -----------------------------------------------------------------------

## Infrastructure & deployment

  -----------------------------------------------------------------------
  Technology           Role
  -------------------- --------------------------------------------------
  Open Liberty         Primary app server (HTTP 9080,
                       HTTPS 9443); `scripts/redeploy-liberty.sh`

  Embedded Tomcat      Alternate local run via Spring Boot
                       (`server.port=8080`)

  PostgreSQL           Persistence (localhost:5432 typical);
                       `bankone` (+ optional `bankone_read`)

  Redis                Local cache + rate-limit store (`:6379`)
  -----------------------------------------------------------------------

## Documentation toolchain

  -----------------------------------------------------------------------
  Technology           Role
  -------------------- --------------------------------------------------
  Markdown             Working copies in `docs/_source/`
  Pandoc               Markdown → DOCX / HTML
  Google Chrome        Headless print → PDF
  python-docx          Post-process DOCX styling
                       (`scripts/_polish_docx.py`)
  regenerate-docs.sh   Rebuild all DOCX + PDF pairs
  -----------------------------------------------------------------------

## IDE / agent conventions (project)

  -----------------------------------------------------------------------
  Item                 Notes
  -------------------- --------------------------------------------------
  Cursor rules         `.cursor/rules/` (docs architect,
                       teach-while-building, deferred hardening)
  Docs ownership       Docs updated with every code change
                       (see `documentation-architect.mdc`)
  -----------------------------------------------------------------------

## How to keep this file current

1.  After changing `pom.xml` or `package.json`, update the matching
    tables above (name + version + purpose).
2.  After changing deploy target, DB, or auth mechanism, update
    **At a glance** and the relevant section.
3.  Append an entry to `CHANGELOG.md`.
4.  Run `BankOne/scripts/regenerate-docs.sh` so DOCX/PDF stay aligned.

Do **not** list planned libraries here until they are actually added to
the build.
