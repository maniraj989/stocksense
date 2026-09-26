# PostgreSQL Database Backup & Restore Guide

This document defines standard operational procedures for performing backups and restores for the **StockSense** PostgreSQL database (`stocksense`).

---

## 1. Prerequisites

Ensure PostgreSQL 16 client tools (`pg_dump`, `pg_restore`, `psql`) are accessible in your environment `PATH` or located under:
- Development: `C:\tools\pgsql\bin\`

Verify command availability:
```powershell
pg_dump --version
pg_restore --version
psql --version
```

Database connection environment variables:
```powershell
$env:PGHOST = "localhost"
$env:PGPORT = "5432"
$env:PGUSER = "postgres"
$env:PGDATABASE = "stocksense"
$env:PGPASSWORD = "your_secure_password" # Or configure pgpass.conf
```

---

## 2. Backup Procedures

### A. Full Schema + Data Backup (Custom Directory/Compressed Format - Recommended)
The PostgreSQL custom format (`-F c`) is compressed, supports parallel multi-threaded restore, and allows flexible selective object restoration.

```powershell
# Create timestamped backup filename
$backupDate = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "backups/stocksense_${backupDate}.dump"

# Ensure backup directory exists
New-Item -ItemType Directory -Force -Path "backups" | Out-Null

# Execute pg_dump
pg_dump -h localhost -p 5432 -U postgres -F c -b -v -f $backupFile stocksense
```

### B. Plain Text SQL Dump (Readable / Portable)
Generates standard SQL statements (DDL + DML) suitable for inspection, diffing, or simple debugging:

```powershell
pg_dump -h localhost -p 5432 -U postgres -F p -v -f "backups/stocksense_${backupDate}.sql" stocksense
```

### C. Schema-Only Backup (DDL only)
To capture only table definitions, constraints, indexes, and Flyway migration state without transactional rows:

```powershell
pg_dump -h localhost -p 5432 -U postgres -s -F p -f "backups/stocksense_schema_${backupDate}.sql" stocksense
```

### D. Data-Only Backup (DML only)
To export table rows without changing schemas:

```powershell
pg_dump -h localhost -p 5432 -U postgres -a --disable-triggers -F c -f "backups/stocksense_data_${backupDate}.dump" stocksense
```

---

## 3. Restore Procedures

### Pre-Restore Safety Check:
> [!CAUTION]
> Never perform automated restores on production without taking a pre-restore backup first and ensuring no active application connections are modifying data.

### A. Restoring Custom Format (`.dump`) into a Clean Database
```powershell
# 1. Terminate active connections (if database already exists)
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'stocksense' AND pid <> pg_backend_pid();"

# 2. Re-create database or drop existing
dropdb -h localhost -p 5432 -U postgres --if-exists stocksense
createdb -h localhost -p 5432 -U postgres -E UTF8 -O postgres stocksense

# 3. Restore schema & data using pg_restore
pg_restore -h localhost -p 5432 -U postgres -d stocksense -v "backups/stocksense_20260926_120000.dump"
```

### B. Restoring Plain Text SQL Backup (`.sql`)
```powershell
psql -h localhost -p 5432 -U postgres -d stocksense -f "backups/stocksense_20260926_120000.sql"
```

---

## 4. Flyway Schema History Considerations

During restoration:
- The `flyway_schema_history` table is included in full database backups.
- When restoring into a fresh database, Flyway detects already applied migrations and avoids re-executing migrations that have already run.
- Checksums in `flyway_schema_history` must match the migration scripts in `src/main/resources/db/migration/`.

Verify post-restore migration integrity:
```powershell
psql -h localhost -p 5432 -U postgres -d stocksense -c "SELECT installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## 5. Verification Checklist Post-Restore

1. **Table Count**: Ensure all 9 base tables exist:
   - `users`, `suppliers`, `products`, `purchases`, `purchase_items`, `sales`, `sale_items`, `stock_movements`, `alerts`.
2. **Row Counts**: Run count queries to verify row restoration matches source.
3. **Foreign Keys & Indexes**: Verify indexes and foreign keys are valid.
4. **Application Health Check**: Start Spring Boot application and query `/api/health`.
