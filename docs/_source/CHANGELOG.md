# CHANGELOG

Append-only history of documentation-relevant product changes.

---

## 2026-07-23 — Rename BanOne-BackEnd → BankOne-BackEnd

| Field | Value |
|-------|-------|
| **Feature** | Fix misspelled backend folder name |
| **Files Modified** | Folder rename; scripts, README, docs, architect rule |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | Correct spelling to match BankOne branding |
| **Impact** | Re-open IntelliJ on `BankOne-BackEnd` |

---

## 2026-07-23 — Move backend into BankOne-BackEnd

| Field | Value |
|-------|-------|
| **Feature** | Relocate Maven `src` + `pom.xml` under `BankOne-BackEnd/` |
| **Files Modified** | `BankOne-BackEnd/**`, `scripts/redeploy-liberty.sh`, `README.md`, docs paths, architect rule |
| **Classes Modified** | N/A (path move only) |
| **Methods Modified** | N/A |
| **Reason** | Separate backend folder alongside `BankOne-Frontend` |
| **Impact** | Open IntelliJ on `BankOne-BackEnd`; redeploy script builds from new root |

---

## 2026-07-23 — Documentation formatting / alignment fix

| Field | Value |
|-------|-------|
| **Feature** | Restyle DOCX + PDF (margins, tables, headings, code blocks) |
| **Files Modified** | `BankOne/docs/**/*.docx`, `**/*.pdf`, `docs/_assets/*`, `scripts/regenerate-docs.sh`, `scripts/_polish_docx.py` |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | User reported poor alignment inside exported docs |
| **Impact** | Open refreshed PDF/DOCX; use regenerate script after content updates |

---

## 2026-07-23 — Docs format: Word + PDF in project folder

| Field | Value |
|-------|-------|
| **Feature** | Replace Markdown docs with DOCX/PDF; move under `BankOne/docs` |
| **Files Modified** | `BankOne/docs/**` (docx+pdf), removed workspace `/docs` markdown; `.cursor/rules/documentation-architect.mdc`; `BankOne/README.md` |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | User requested PDF/Word instead of MD, located in the project folder |
| **Impact** | Architect rule treats `.docx` as editable source and `.pdf` as distribution copy |

---

## 2026-07-23 — Documentation baseline (Software Architect)

| Field | Value |
|-------|-------|
| **Feature** | Establish living `/docs` synchronized with codebase |
| **Files Modified** | `docs/**`, `.cursor/rules/documentation-architect.mdc` |
| **Classes Modified** | N/A (docs only) |
| **Methods Modified** | N/A |
| **Reason** | Permanent architect mandate: docs are part of source; capture current implemented state |
| **Impact** | All future features must update these docs + append this changelog |

---

## 2026-07-23 — Account policies GET + opening deposit UX

| Field | Value |
|-------|-------|
| **Feature** | Policy-aware opening deposit for Current accounts |
| **Files Modified** | Backend: `AccountPolicyController`, `AccountPolicyService`/`Impl`; Frontend: `account-policy.ts`, `opening-deposit-dialog/*`, `customer-detail.ts` |
| **Classes Modified** | `AccountPolicyController`, `AccountPolicyServiceImpl`, Angular `OpeningDepositDialog`, `CustomerDetail` |
| **Methods Modified** | `getActivePolicy()`, `addCurrentAccount()`, dialog submit → `openAccount` |
| **Reason** | CURRENT/INR policy requires min opening deposit (seed 5000); UI must not hardcode `1` |
| **Impact** | Customer detail Current flow; account open validation; docs `MODULES/Account.md`, API, sequences |

---

## 2026-07-22 — Accounts list + deposit API

| Field | Value |
|-------|-------|
| **Feature** | Accounts search UI + `POST /accounts/{id}/deposit` |
| **Files Modified** | `AccountController`, `AccountServiceImpl`, `DepositRequest`, frontend `features/accounts/*`, sidebar route |
| **Classes Modified** | `AccountController`, `AccountServiceImpl`, `AccountList` |
| **Methods Modified** | `searchAccounts()`, `deposit()` |
| **Reason** | Staff can find accounts and credit balances |
| **Impact** | No ledger rows yet; Transaction module still stub |

---
