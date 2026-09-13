# Clearly Admin Panel

Open `http://127.0.0.1:4173/admin/` while the local server is running.

The panel currently uses the existing local product API and JSON store so it works immediately in this demo. It includes:

- product image upload and preview
- product document upload, including PDF files
- flexible customer-facing Measure & dilute / How to use text
- bulk discount enable/disable toggle
- package quantity and discount percentage controls
- a MySQL table preview before database creation
- a protected Users view backed by the authentication database

## Users access

The Users screen uses the JWT created by the store account page. Only accounts
whose `users.role` is `ADMIN` can load customer records. To nominate the first
administrator after signup and OTP verification, update that account once:

```sql
UPDATE clearly_store.users SET role = 'ADMIN' WHERE email = 'owner@example.com';
```

Sign out and sign in again so the new JWT contains the administrator role.
The portal intentionally never receives `password_hash`, OTP hashes, or Google
subject identifiers.

The proposed MySQL schema is in `mysql-schema.sql`. Review it in the Database preview tab before running it against a MySQL instance. No database is created automatically.
