# Extension Guide

## Adding a new domain module (backend)

1.  Create package under `com.bankone.<module>/` with `controller`,
    `service`, `repository`, `entity`, `dto` as needed.
2.  Wire Spring components (`@RestController`, `@Service`,
    `@Repository`).
3.  Add `SecurityConfig` matchers (do not rely on "authenticated only"
    for admin mutations).
4.  Add entity → verify Hibernate update / add Flyway later.
5.  Seed data via `ApplicationRunner` if required.
6.  Document: new `docs/MODULES/<Name>.md`, API, schema, call flow,
    CHANGELOG.

## Adding a new Angular feature

1.  Generate/create under `src/app/features/<name>/`.
2.  Add route in `app.routes.ts` with `canActivate` + `data.roles`.
3.  Add sidebar entry in `core/layout/sidebar/` **only when route is
    real**.
4.  Add `core/services/<name>.ts` + models.
5.  Update module docs + CHANGELOG.

## Extending accounts safely

  ------------------------------------------------------------
  Goal         Recommended approach
  ------------ -----------------------------------------------
  New account  Add `AccountType` enum value + policy seed + UI
  type         label; do not invent numbers without generator
               update

  Opening      Prefer `account_policy` row over hardcoding
  deposit      
  change       

  Ledger /     Introduce `transaction` entity; call from
  withdraw     deposit/withdraw/open; stop mutating balances
               alone

  Branch       New `branch` table; replace free-text
  master       `branchCode` gradually
  ------------------------------------------------------------

## Extending security

- Put roles in matchers early.
- Prefer method security `@PreAuthorize` for fine-grained ops.
- Keep JWT secret out of git for production (deferred hardening).

## Database evolution path

Today: `ddl-auto=update` + `schema.sql` for sequences.\
Target: Flyway/Liquibase migrations under
`src/main/resources/db/migration`, set `ddl-auto=validate`.

## Stub modules ready for first implementation

  ------------------------------------------------------------------------
  Module        Empty package               Suggested first slice
  ------------- --------------------------- ------------------------------
  Transaction   `com.bankone.transaction`   CREDIT entry on deposit + open

  Report        `com.bankone.report`        Daily deposits summary

  Audit         `com.bankone.audit`         Persist security/business
                                            events

  Beneficiary   `com.bankone.beneficiary`   Payee CRUD for transfers
  ------------------------------------------------------------------------

## Documentation template for new features

Every new feature doc must include sections 1--20 listed in
`docs/README.md` / architect rule, plus `# Future Modification Guide`
with files, classes, methods, impact, and call hierarchy.
