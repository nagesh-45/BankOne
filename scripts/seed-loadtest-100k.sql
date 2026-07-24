-- =============================================================================
-- BankOne load-test seed: 100,000 customers + accounts + employees
-- =============================================================================
-- Spot this data in the UI / SQL:
--   Customers : LoadTest Customer000001 … Customer100000
--   Email     : loadtest.customer000001@bankone.test
--   Phone     : 7000000001 … 7000100000
--   Branch    : 9999  |  account created_by = LOADTEST
--   Employees : username loadtest.emp000001 … loadtest.emp100000
--               password LoadTest@123  (shared BCrypt hash below)
--
-- Replaces any previous LOADTEST / loadtest.* seed (10k or 100k).
-- Does NOT touch the real admin user.
--
-- Run (local):
--   psql -h localhost -U bankone_user -d bankone -f scripts/seed-loadtest-100k.sql
--
-- Run (Render External URL):
--   psql "postgresql://USER:PASS@HOST:5432/DB" -f scripts/seed-loadtest-100k.sql
--
-- Expect several minutes over Render External; keep the session open.
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 0) Remove previous load-test rows (safe to re-run)
-- ---------------------------------------------------------------------------
DELETE FROM bank_transaction
WHERE account_id IN (
    SELECT account_id FROM account WHERE created_by = 'LOADTEST' OR branch_code = '9999'
);

DELETE FROM account
WHERE created_by = 'LOADTEST' OR branch_code = '9999';

DELETE FROM customers
WHERE email LIKE 'loadtest.customer%@bankone.test'
   OR (first_name = 'LoadTest' AND last_name LIKE 'Customer%');

DELETE FROM user_roles
WHERE user_id IN (
    SELECT user_id FROM users
    WHERE username LIKE 'loadtest.emp%'
       OR email LIKE 'loadtest.emp%@bankone.test'
);

DELETE FROM users
WHERE username LIKE 'loadtest.emp%'
   OR email LIKE 'loadtest.emp%@bankone.test';

-- ---------------------------------------------------------------------------
-- 1) Helper: Mod-97 check digits (same algorithm as AccountNumberGenerator)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION bankone_mod97_check(base text)
RETURNS text
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    rem bigint := 0;
    i   int;
    cd  int;
BEGIN
    IF base IS NULL OR base !~ '^\d+$' THEN
        RAISE EXCEPTION 'base must be digits only: %', base;
    END IF;

    FOR i IN 1..length(base) LOOP
        rem := (rem * 10 + substring(base FROM i FOR 1)::int) % 97;
    END LOOP;

    cd := 98 - rem::int;
    IF cd = 98 THEN
        cd := 0;
    END IF;

    RETURN lpad(cd::text, 2, '0');
END;
$$;

-- ---------------------------------------------------------------------------
-- 2) Insert 100,000 customers
-- ---------------------------------------------------------------------------
INSERT INTO customers (
    first_name,
    last_name,
    email,
    phone_number,
    date_of_birth,
    address,
    status,
    created_at,
    updated_at
)
SELECT
    'LoadTest',
    'Customer' || lpad(n::text, 6, '0'),
    'loadtest.customer' || lpad(n::text, 6, '0') || '@bankone.test',
    (7000000000 + n)::text,
    DATE '1990-01-01' + ((n - 1) % 10000),
    'LoadTest Address #' || lpad(n::text, 6, '0') || ', BankOne Test City',
    'ACTIVE',
    NOW(),
    NOW()
FROM generate_series(1, 100000) AS n;

-- ---------------------------------------------------------------------------
-- 3) Linked accounts (branch 9999)
--    ~100k SAVINGS + ~20k CURRENT (every 5th) ≈ 120k accounts
-- ---------------------------------------------------------------------------
WITH seeded AS (
    SELECT
        c.customer_id,
        substring(c.email FROM 'loadtest\.customer(\d+)@')::int AS n
    FROM customers c
    WHERE c.email LIKE 'loadtest.customer%@bankone.test'
),
account_rows AS (
    SELECT
        s.customer_id,
        s.n,
        '9999'::text AS branch_code,
        'SAVINGS'::text AS account_type,
        '01'::text AS type_code,
        (90000000 + s.n)::bigint AS ordinal,
        'INR'::text AS currency_code,
        '356'::text AS currency_num,
        (1000 + (s.n % 9000))::numeric(19, 2) AS balance
    FROM seeded s

    UNION ALL

    SELECT
        s.customer_id,
        s.n,
        '9999'::text,
        'CURRENT'::text,
        '02'::text,
        (91000000 + s.n)::bigint,
        'INR'::text,
        '356'::text,
        (5000 + (s.n % 5000))::numeric(19, 2)
    FROM seeded s
    WHERE s.n % 5 = 0
)
INSERT INTO account (
    account_number,
    branch_code,
    account_type,
    ordinal,
    currency_code,
    check_digit,
    available_balance,
    ledger_balance,
    debit_count,
    credit_count,
    last_credit_at,
    last_transaction_at,
    status,
    created_at,
    activated_at,
    created_by,
    customer_id
)
SELECT
    base || bankone_mod97_check(base) AS account_number,
    ar.branch_code,
    ar.account_type,
    ar.ordinal::int,
    ar.currency_code,
    right(bankone_mod97_check(base), 1)::int,
    ar.balance,
    ar.balance,
    0,
    1,
    NOW(),
    NOW(),
    'ACTIVE',
    NOW(),
    NOW(),
    'LOADTEST',
    ar.customer_id
FROM account_rows ar
CROSS JOIN LATERAL (
    SELECT
        ar.branch_code
        || ar.type_code
        || lpad(ar.ordinal::text, 8, '0')
        || ar.currency_num AS base
) b;

-- ---------------------------------------------------------------------------
-- 4) Advance account_ordinal_seq past load-test ordinals
-- ---------------------------------------------------------------------------
SELECT setval(
    'account_ordinal_seq',
    GREATEST(
        (SELECT COALESCE(MAX(ordinal), 1) FROM account),
        (SELECT last_value FROM account_ordinal_seq)
    ),
    true
);

-- ---------------------------------------------------------------------------
-- 5) Insert 100,000 employees (users + EMPLOYEE role)
--    Shared password: LoadTest@123
--    BCrypt (Spring-compatible $2a$): generated via htpasswd -nbBC 10
-- ---------------------------------------------------------------------------
INSERT INTO users (
    username,
    password,
    first_name,
    last_name,
    email,
    enabled,
    account_locked,
    credentials_expired,
    failed_login_attempts,
    last_login,
    password_changed_at,
    created_at,
    updated_at,
    version
)
SELECT
    'loadtest.emp' || lpad(n::text, 6, '0'),
    '$2a$10$QQ2QN3KTJlHanLD17JmuKe1ezuZ.j.FH3aWRITfkLWnMi/5indu1K',
    'LoadTest',
    'Employee' || lpad(n::text, 6, '0'),
    'loadtest.emp' || lpad(n::text, 6, '0') || '@bankone.test',
    TRUE,
    FALSE,
    FALSE,
    0,
    NULL,
    NOW(),
    NOW(),
    NOW(),
    0
FROM generate_series(1, 100000) AS n;

INSERT INTO user_roles (
    user_id,
    role_id,
    role_name,
    active,
    created_at,
    updated_at,
    version
)
SELECT
    u.user_id,
    r.role_id,
    'EMPLOYEE',
    TRUE,
    NOW(),
    NOW(),
    0
FROM users u
CROSS JOIN roles r
WHERE u.username LIKE 'loadtest.emp%'
  AND r.role_name = 'EMPLOYEE';

-- Keep Hibernate sequences ahead of bulk IDs
SELECT setval(
    'user_seq',
    GREATEST(
        (SELECT COALESCE(MAX(user_id), 1) FROM users),
        (SELECT last_value FROM user_seq)
    ),
    true
);

SELECT setval(
    'user_role_seq',
    GREATEST(
        (SELECT COALESCE(MAX(user_role_id), 1) FROM user_roles),
        (SELECT last_value FROM user_role_seq)
    ),
    true
);

-- ---------------------------------------------------------------------------
-- 6) Summary
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    cust_count int;
    acct_count int;
    emp_count  int;
BEGIN
    SELECT COUNT(*) INTO cust_count
    FROM customers
    WHERE email LIKE 'loadtest.customer%@bankone.test';

    SELECT COUNT(*) INTO acct_count
    FROM account
    WHERE created_by = 'LOADTEST';

    SELECT COUNT(*) INTO emp_count
    FROM users
    WHERE username LIKE 'loadtest.emp%';

    RAISE NOTICE 'Load-test 100k seed complete: % customers, % accounts, % employees',
        cust_count, acct_count, emp_count;
    RAISE NOTICE 'Login sample employee: loadtest.emp000001 / LoadTest@123';
    RAISE NOTICE 'Search UI for: LoadTest  |  branch_code = 9999';
END $$;

COMMIT;

-- Optional cleanup later (do not run with the seed):
-- DELETE FROM bank_transaction WHERE account_id IN (SELECT account_id FROM account WHERE created_by = 'LOADTEST');
-- DELETE FROM account WHERE created_by = 'LOADTEST';
-- DELETE FROM customers WHERE email LIKE 'loadtest.customer%@bankone.test';
-- DELETE FROM user_roles WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE 'loadtest.emp%');
-- DELETE FROM users WHERE username LIKE 'loadtest.emp%';
-- DROP FUNCTION IF EXISTS bankone_mod97_check(text);
