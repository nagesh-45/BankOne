# Database Schema

## Management approach

  -------------------------------------------------------------------
  Mechanism                              Role
  -------------------------------------- ----------------------------
  Hibernate `ddl-auto=update`            Creates/updates most tables
                                         from entities

  `schema.sql`                           Creates
                                         `account_ordinal_seq`

  Seed runners                           `RoleInitializer`,
                                         `AdminRoleInitializer`,
                                         `AccountPolicyInitializer`
  -------------------------------------------------------------------

## Entity-relationship (current)

```
erDiagram
  customers ||--o{ account : has
  users ||--o{ user_roles : has
  roles ||--o{ user_roles : grants
  account_policy {
    bigint policy_id PK
    string account_type
    string currency_code
    boolean opening_deposit_required
    decimal required_opening_deposit
    decimal minimum_balance
    boolean active
  }
  customers {
    bigint customer_id PK
    string first_name
    string last_name
    string email UK
    string phone_number UK
    date date_of_birth
    string address
    string status
    timestamp created_at
    timestamp updated_at
  }
  account {
    bigint account_id PK
    string account_number UK
    string branch_code
    string account_type
    int ordinal
    string currency_code
    int check_digit
    decimal available_balance
    decimal ledger_balance
    int debit_count
    int credit_count
    string status
    bigint customer_id FK
  }
  users {
    bigint user_id PK
    string username UK
    string password_hash
    boolean enabled
    int failed_attempts
    boolean locked
  }
  roles {
    bigint role_id PK
    string name UK
  }
  user_roles {
    bigint user_role_id PK
    bigint user_id FK
    bigint role_id FK
  }
```

## Tables

### `customers`

Entity: `com.bankone.customer.entity.Customer`

  -------------------------------------------------------------
  Column                         Notes
  ------------------------------ ------------------------------
  `customer_id`                  PK, identity

  `first_name`, `last_name`      Required

  `email`, `phone_number`        Unique

  `date_of_birth`                Optional

  `address`, `status`            Required

  `created_at`, `updated_at`     `@PrePersist` / `@PreUpdate`
  -------------------------------------------------------------

`customerCode` is **not** stored; JSON accessor formats ID via
`BusinessIdFormatter`.

### `account`

Entity: `com.bankone.account.entity.Account`

  -------------------------------------------------------------
  Column                           Notes
  -------------------------------- ----------------------------
  `account_id`                     PK

  `account_number`                 Unique, generated

  `branch_code`                    String (no branch table)

  `account_type`, `currency_code`, Number composition
  `ordinal`, `check_digit`         

  `available_balance`,             Updated on open/deposit
  `ledger_balance`                 

  `debit_count`, `credit_count`,   Counters / stamps
  `last_*_at`                      

  `status`                         ACTIVE, FROZEN, DORMANT,
                                   SUSPENDED, CLOSED

  `customer_id`                    FK → `customers`

  Audit-ish                        `created_at`,
                                   `activated_at`, `closed_at`,
                                   `created_by`, `closed_by`
  -------------------------------------------------------------

### `account_policy`

Entity: `com.bankone.account.entity.AccountPolicy` extends
`AuditableEntity`

Unique `(account_type, currency_code)`.

Seeded INR policies for CURRENT, SAVINGS, SALARY, FIXED_DEPOSIT,
RECURRING_DEPOSIT. **No LOAN seed.**

### `users`, `roles`, `user_roles`

Entities under `com.bankone.user` / `com.bankone.role`. Sequences:
`user_seq`, `role_seq`, `user_role_seq` (Hibernate-managed naming may
vary by dialect config).

`AuditableEntity` columns on User/Role/UserRole/AccountPolicy:
`created_at`, `updated_at`, `created_by`, `updated_by`, `version`.

## Sequences

    -- schema.sql
    CREATE SEQUENCE IF NOT EXISTS account_ordinal_seq
    START WITH 1 INCREMENT BY 1;

Used by `AccountRepository.getNextOrdinal()`.

## Not present (planned)

No tables yet for: `transaction` / ledger entries, `branch`, `loan`
product, `audit_event`, `beneficiary`.

## Migration notes

Changing entity fields with `ddl-auto=update` alters tables in place.
For production-ready history, replace with Flyway/Liquibase (see
EXTENSION_GUIDE).
