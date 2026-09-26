# StockSense — Security Architecture & RBAC Design

This document details the Spring Security 6, BCrypt, and Role-Based Access Control (RBAC) architecture implemented in **Phase 5** for the **StockSense** system.

---

## 1. Security Architecture Overview

StockSense utilizes a 100% Java-based security stack integrating **Spring Security 6**, **BCrypt**, and **Vaadin 24 Flow Web Security**.

```text
HTTP Request
     │
     ├── Vaadin Flow Internal / Web Requests
     │         │
     │         ▼
     │   VaadinWebSecurity (UIDL CSRF, Navigation Access Control, Vaadin DevBundle)
     │
     └── REST / API Requests (/api/**)
               │
               ▼
         SecurityFilterChain
               │
               ├── /api/health ────────► Public (Permit All)
               ├── /api/admin/** ──────► Requires Authority: ROLE_ADMIN
               └── /api/** ────────────► Requires Authentication (Authenticated Principal)
```

---

## 2. Authentication Flow & Legacy Password Upgrade Strategy

Existing databases and imports from legacy MySQL may contain plaintext passwords (e.g. `admin123` / `staff123`). StockSense addresses this without mass-converting or invalidating accounts:

```text
User Submits Username + Password
               │
               ▼
UpgradePasswordAuthenticationProvider
               │
               ▼
Load UserDetails from CustomUserDetailsService (UserRepository)
               │
               ▼
Is Stored Password a BCrypt Hash ($2a$, $2b$, $2y$)?
      ├── YES ──► Verify using BCryptPasswordEncoder.matches(raw, stored)
      │
      └── NO (Legacy Format)
               │
               ▼
         Does stored plaintext match submitted password?
               ├── YES ──► 1. Compute new BCrypt hash
               │           2. Transparently update User entity in PostgreSQL
               │           3. Log migration event (no plaintext logged)
               │           4. Authenticate user successfully
               │
               └── NO  ──► Throw BadCredentialsException
```

---

## 3. Role-Based Access Control (RBAC)

* **Domain Roles (`UserRole`)**:
  - `ADMIN`: Full administrative control (user management, system configuration, master catalog).
  - `STAFF`: Operational inventory, sales, purchases, alert views.
* **Spring Security Authorities**:
  - Standard role prefix: `ROLE_ADMIN` and `ROLE_STAFF`.
* **Method-Level Security**:
  - Enabled via `@EnableMethodSecurity(prePostEnabled = true)`.
  - Administrative operations protected by `@PreAuthorize("hasRole('ADMIN')")`.

---

## 4. Password Policy

Centralized in `PasswordPolicy`:
* Minimum length: 8 characters.
* Maximum length: 128 characters.
* Rejection of null, empty, or whitespace-only values.
* BCrypt hashing using 10 rounds of work factor.

---

## 5. Session Management & CSRF Strategy

* **CSRF Protection**:
  - Active for all standard browser forms and Vaadin server-push/UIDL interactions.
  - Exempted for `/api/**` to facilitate headless/programmatic API integrations using Basic authentication.
* **Session Protection**:
  - Vaadin server-side session management with session fixation protection.
  - Generic authentication messages prevent username enumeration during failed logins.

---

## 6. Coexistence with Legacy System

* Legacy JDBC DAOs in [`src/com/inventory/dao/`](file:///D:/stockesense-main/src/com/inventory/dao/) continue to function for the Swing application without modification.
* The original schema ([`sql/smart_inventory.sql`](file:///D:/stockesense-main/sql/smart_inventory.sql)) and Flyway V1 ([`V1__initial_postgresql_schema.sql`](file:///D:/stockesense-main/src/main/resources/db/migration/V1__initial_postgresql_schema.sql)) are preserved untouched.
