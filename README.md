# Hiper Market Web

This is a responsive PHP/HTML/CSS/JavaScript web client and JSON API for the
existing Hiper Market database schema. It is a new web interface; it does not
change or replace the desktop application.

## What is included

- Responsive navigation and overview with API/database health and store metrics.
- Four primary destinations: Home, POS Terminal, User Profile, and Application
  Settings. Small screens use a fixed bottom navigation bar.
- Sign-in against `Users`, expiring bearer tokens, sign-out, and manager-gated
  product, user, and shop record creation/updates.
- The signed-out security panel is shown inside User Profile; opening the site
  does not force a login dialog.
- Product search and inventory display, sales history, shop list, and daily
  sales report.
- POS cart, quantity/stock checks, payment capture, sale persistence, stock
  decrement, and printable receipt.
- Authenticated image uploads (JPG, PNG, WEBP, GIF; 5 MB maximum).
- `API/API.php` checks that the server can reach the configured database and
  that the expected application tables are present.

It is not yet a feature-for-feature port of every desktop screen. Existing
desktop workflows such as barcode label design, printer selection, offline
sync, complex document editing, detailed permission levels, and shop slip
layout configuration still need dedicated web implementations.

## Requirements

- PHP 8.1 or later.
- PDO and the PDO driver for the configured database (`pdo_sqlite` or
  `pdo_mysql`); `fileinfo` for upload MIME validation.
- The server database must already have the desktop application's `Users`,
  `Shops`, `product`, and `doc_table` tables and columns.
- HTTPS for any non-local deployment.

## Configure the server

Configure these environment variables in the PHP hosting environment:

- `HIPER_DB_DSN`: PDO DSN. Defaults to SQLite at the existing repository's
  `data/my_database.db` path. For MySQL, use a DSN such as
  `mysql:host=127.0.0.1;dbname=hipermarket;charset=utf8mb4`.
- `HIPER_DB_USER` and `HIPER_DB_PASSWORD`: database credentials when required.
- `HIPER_TOKEN_SECRET`: a long, random server-only secret. Login and API-token
  authentication stay disabled until this is configured.
- `HIPER_UPLOAD_DIR`: optional writable directory for uploaded images. Defaults
  to `WebApp/uploads`.
- `HIPER_ALLOWED_ORIGIN`: optional exact origin for a separately hosted
  frontend. Same-origin deployment does not need CORS.

Never put database credentials or `HIPER_TOKEN_SECRET` into browser JavaScript
or commit them to source control. The PHP API owns the connection and confirms
its health at `API/API.php?action=health`. Deploy the `WebApp` directory as the
site document root and point the configured database path/DSN at the existing
application database. Do not expose the repository root or `.venv` through a
public web server.

For local PHP development, from the repository root run:

```powershell
$env:HIPER_TOKEN_SECRET = "replace-with-a-long-random-local-secret"
$env:HIPER_DB_PATH = (Resolve-Path ".\data\my_database.db").Path
php -S 127.0.0.1:8080 -t .\WebApp
```

Open `http://127.0.0.1:8080` to load `index.php`; the health status should
confirm the API/database before sign-in. This sample is for a trusted local
machine only; use HTTPS and hosting-managed environment secrets for a shared or
production server.

## API routes

All routes are served by `API/API.php`; JSON requests use `action` in the query
string or JSON body. Authenticated routes require `Authorization: Bearer <token>`.

- `GET ?action=health` — test the database and required tables.
- `POST ?action=login` — username/email and password; returns a 12-hour token.
- `POST ?action=logout` and `GET ?action=me`.
- `GET ?action=dashboard`, `?action=list&resource=products|users|shops|sales`,
  and `GET ?action=reports`.
- `POST ?action=save` — manager-only product, user, or shop record create/update.
- `POST ?action=sale` — save a sale and decrement stock in one transaction.
- `POST ?action=upload` — multipart image upload under field name `file`.
