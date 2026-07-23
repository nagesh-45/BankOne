# BankOne

Monorepo folder:

| Path | Contents |
|------|----------|
| `BankOne-BackEnd/` | Spring Boot 4 / Java 21 WAR (`pom.xml`, `src/`) |
| `BankOne-Frontend/` | Angular SPA |
| `docs/` | Word + PDF documentation |
| `scripts/` | Liberty redeploy, docs regenerate |

## Quick start

```bash
# Backend (from BankOne-BackEnd)
cd BankOne-BackEnd && ./mvnw spring-boot:run
# or Liberty
../scripts/redeploy-liberty.sh

# Frontend
cd BankOne-Frontend && ng serve
```

Docs: start with `docs/README.pdf` (or `.docx`).
