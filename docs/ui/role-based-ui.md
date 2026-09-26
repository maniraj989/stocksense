# Role-Based Access Control in StockSense UI

## Architectural Principle
UI visibility is convenient, but **server-side authorization is always authoritative**.
The UI never relies solely on hiding elements or links. All actions and views are enforced with Spring Security annotations (`@RolesAllowed`, `@PreAuthorize`) and backend service layer security.

---

## Permission Matrix

| Capability / Route | Anonymous | `ROLE_STAFF` | `ROLE_ADMIN` | Enforcement Mechanism |
| :--- | :---: | :---: | :---: | :--- |
| Login Screen (`/login`) | YES | YES | YES | `@AnonymousAllowed` |
| Public Health (`/api/health`) | YES | YES | YES | `SecurityConfig` `permitAll()` |
| Dashboard (`/`, `/dashboard`) | NO | YES | YES | `@PermitAll` |
| View Products (`/products`) | NO | YES | YES | `@PermitAll` |
| Create / Edit Product | NO | YES | YES | `@PermitAll` |
| Delete Product | NO | NO | YES | `@PreAuthorize("hasRole('ADMIN')")` in Service & Controller |
| View Suppliers (`/suppliers`) | NO | YES | YES | `@PermitAll` |
| Create / Edit Supplier | NO | YES | YES | `@PermitAll` |
| Delete Supplier | NO | NO | YES | `@PreAuthorize("hasRole('ADMIN')")` in Service & Controller |
| Create Purchase Order (`/purchases`) | NO | YES | YES | `@PermitAll` |
| Complete Sale / POS (`/sales`) | NO | YES | YES | `@PermitAll` |
| Stock Overview & Adjustment (`/stock`) | NO | YES | YES | `@PermitAll` |
| Alerts Management (`/alerts`) | NO | YES | YES | `@PermitAll` |
| User Profile & Password (`/profile`) | NO | YES | YES | `@PermitAll` |
| Admin User Management (`/admin/users`) | NO | NO | YES | `@RolesAllowed("ADMIN")` on Route + `hasRole('ADMIN')` on API |
| Create / Deactivate System Users | NO | NO | YES | `UserService` `@PreAuthorize("hasRole('ADMIN')")` |

---

## Navigation UI Adaptation
In `MainLayout.java`:
- The navigation link to `Administration (Users)` is only added if `SecurityUtils.isAdmin()` returns `true`.
- If a `STAFF` user manually navigates to `/admin/users`, Vaadin Flow and Spring Security immediately deny access and show an access-denied error without rendering the view.
