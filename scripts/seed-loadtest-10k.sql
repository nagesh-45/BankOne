-- =============================================================================
-- BankOne load-test seed: 10,000 customers + linked accounts
-- =============================================================================
-- How to spot this data in the UI / SQL:
--   Customer name : LoadTest Customer00001 … LoadTest Customer10000
--   Email         : loadtest.customer00001@bankone.test
--   Phone         : 7000000001 … 7000010000
--   Address       : LoadTest Address #00001, BankOne Test City
--   Branch        : 9999  (load-test branch — filter accounts by this)
--   created_by    : LOADTEST
--   Account types : SAVINGS for everyone; CURRENT also on every 5th customer
--
-- Run against local Postgres (Liberty default):
--   psql -h localhost -U bankone_user -d bankone -f scripts/seed-loadtest-10k.sql
--
-- Or from psql prompt:
--   \i /path/to/BankOne/scripts/seed-loadtest-10k.sql
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
-- 2) Insert 10,000 customers
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
    'Customer' || lpad(n::text, 5, '0'),
    'loadtest.customer' || lpad(n::text, 5, '0') || '@bankone.test',
    (7000000000 + n)::text,                          -- 10 digits, unique
    DATE '1990-01-01' + ((n - 1) % 10000),           -- spread DOBs
    'LoadTest Address #' || lpad(n::text, 5, '0') || ', BankOne Test City',
    'ACTIVE',
    NOW(),
    NOW()
FROM generate_series(1, 10000) AS n;

-- ---------------------------------------------------------------------------
-- 3) Linked accounts (branch 9999, ordinals 90000001+)
--    Format: BBBB TT OOOOOOOO CCC DD  (19 digits)
-- ---------------------------------------------------------------------------
WITH seeded AS (
    SELECT
        c.customer_id,
        substring(c.email FROM 'loadtest\.customer(\d{5})@')::int AS n
    FROM customers c
    WHERE c.email LIKE 'loadtest.customer%@bankone.test'
),
account_rows AS (
    -- One SAVINGS account per customer
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

    -- Extra CURRENT account on every 5th customer (easy sample of multi-account)
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
    -- App stores only the last digit of the 2-digit check (matches AccountServiceImpl)
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
-- 4) Advance account_ordinal_seq past load-test ordinals (avoid collisions)
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
-- 5) Summary
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    cust_count int;
    acct_count int;
    sample_email text;
    sample_acct  text;
BEGIN
    SELECT COUNT(*) INTO cust_count
    FROM customers
    WHERE email LIKE 'loadtest.customer%@bankone.test';

    SELECT COUNT(*) INTO acct_count
    FROM account
    WHERE created_by = 'LOADTEST';

    SELECT email INTO sample_email
    FROM customers
    WHERE email = 'loadtest.customer00001@bankone.test';

    SELECT account_number INTO sample_acct
    FROM account
    WHERE created_by = 'LOADTEST'
    ORDER BY ordinal
    LIMIT 1;

    RAISE NOTICE 'Load-test seed complete: % customers, % accounts', cust_count, acct_count;
    RAISE NOTICE 'Search UI for: LoadTest  |  email: %  |  sample account: %', sample_email, sample_acct;
    RAISE NOTICE 'Filter accounts by branch_code = 9999';
END $$;

COMMIT;

-- Optional cleanup later (do not run with the seed):
-- DELETE FROM bank_transaction WHERE account_id IN (SELECT account_id FROM account WHERE created_by = 'LOADTEST');
-- DELETE FROM account WHERE created_by = 'LOADTEST';
-- DELETE FROM customers WHERE email LIKE 'loadtest.customer%@bankone.test';
-- DROP FUNCTION IF EXISTS bankone_mod97_check(text);
