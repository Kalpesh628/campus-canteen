# Campus Canteen — Pre-Order & Pickup System

A full-stack Java mini project (FSJP subject): students browse the canteen menu,
add items to a cart, and pre-order for a pickup time slot. Canteen staff (admin)
manage the menu, accept/advance orders through a status pipeline, manage pickup
slots, and view feedback and sales stats. Payment is **Pay at Canteen** — no real
payments are involved.

## Stack (exact combo)

| Layer    | Choice |
|----------|--------|
| Language | Java 17 (Eclipse Temurin 17.0.20.1) |
| Build    | Apache Maven 3.9.9, WAR packaging |
| Backend  | Jakarta Servlet 5.0 (`jakarta.servlet-api`, `@WebServlet`), JSP + JSTL 2.0, JDBC |
| Database | MySQL 8 (`mysql-connector-j` 8.0.33) |
| Server   | Apache Tomcat 10.1.x (Servlet 5.0 / Jakarta namespace) |
| Frontend | HTML5 + CSS3 + JavaScript + Bootstrap 5.3.3 (CDN) |

> **Why this combo:** Java 17 is an LTS. Jakarta Servlet 5.0 is the first
> `jakarta.*` namespace release, and Tomcat 10.1 is its reference implementation —
> so `jakarta.servlet.*` imports compile against Tomcat 10.1's own
> `servlet-api.jar`. (Tomcat 9 uses the old `javax.servlet` namespace and would
> need different imports.)

## Prerequisites

- JDK 17+ (`java -version`)
- Apache Maven 3.9+ (`mvn -version`)
- MySQL 8 server running locally
- Apache Tomcat 10.1.x

## 1. MySQL setup

```bash
mysql -u root -p < schema.sql
```

This creates the `campus_canteen` database, all tables, an app login
`'canteen'@'localhost'` / `canteen123`, one admin, sample menu items, and pickup
slots for today + 2 days. The app reads these credentials from
`src/main/resources/db.properties` — edit that file if your MySQL user differs,
then rebuild.

**Default admin (local dev only):** `admin@canteen.local` / `admin123` — change it after first login.
**Production:** set `ADMIN_EMAIL` and `ADMIN_PASSWORD` env vars (e.g. Railway Variables);
on boot the app creates/repoints the admin account with a freshly generated salt+hash.
The password is never stored in the repo.

## 2. Build

```bash
cd campus-canteen
mvn package
```

This produces `target/campus-canteen.war`.

> **Build note (sandbox network):** this environment reaches Maven Central only
> through an egress proxy that Maven's HTTP client can't tunnel through, so the
> dependencies were pre-fetched into `~/workspace/m2repo` (see `fetch-deps.py`
> / `fetch-one.py`). If a plain `mvn package` fails on dependency downloads,
> build offline instead:
>
> ```bash
> mvn -o -Dmaven.repo.local=$HOME/workspace/m2repo -Dmaven.legacyLocalRepo=true package
> ```
>
> On a normal machine with direct internet, plain `mvn package` just works.

## 3. Deploy to Tomcat

```bash
cp target/campus-canteen.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

Open **http://localhost:8080/campus-canteen/** — it redirects to the menu.
Admin panel: **http://localhost:8080/campus-canteen/admin** (login as admin).

## Project structure

```
campus-canteen/
├── pom.xml                              # Maven: Java 17, WAR, servlet/JSTL/MySQL deps
├── schema.sql                           # DB + tables + seed admin/menu/slots
├── README.md / VIVA_NOTES.md
└── src/main/
    ├── java/com/canteen/
    │   ├── model/       # POJOs: User, MenuItem, PickupSlot, Order, OrderItem, Feedback, CartItem
    │   ├── dao/         # JDBC: UserDAO, MenuItemDAO, PickupSlotDAO, OrderDAO, FeedbackDAO
    │   ├── util/        # DBUtil (connections), PasswordUtil (SHA-256 + salt)
    │   ├── servlet/     # Auth, Menu, Cart, Order, Slot, Feedback + 5 admin servlets
    │   └── filter/      # AuthFilter (login required), AdminFilter (role check)
    ├── resources/db.properties          # DB url/user/password (edit me)
    └── webapp/
        ├── index.jsp                    # -> redirects to /menu
        ├── css/style.css
        └── WEB-INF/
            ├── web.xml                  # filters (ordered!), welcome file, error pages, session timeout
            └── jsp/
                ├── inc/header.jsp       # Bootstrap navbar, JSTL taglibs
                ├── inc/footer.jsp
                ├── error.jsp            # friendly error page (no stack traces)
                ├── login.jsp / register.jsp
                ├── menu.jsp / cart.jsp / checkout.jsp / track.jsp / history.jsp / feedback.jsp
                └── admin/               # dashboard, menu, orders, order-detail, slots, feedback
```

## How a request flows (JSP → Servlet → DAO → DB)

Example — student places an order:

1. **JSP (View):** `checkout.jsp` renders the slot radio list (fetched live via
   `/slots?date=...` JSON) and POSTs `slotId` + note to `/checkout`.
2. **Filter:** `AuthFilter` (web.xml) rejects the request if no `user` in session.
3. **Servlet (Controller):** `OrderServlet.doPost` validates input, recomputes the
   total from the session cart (never trusts the browser), and calls the DAO.
4. **DAO (Model):** `OrderDAO.placeOrder()` opens ONE transaction: it locks the
   slot row (`SELECT ... FOR UPDATE`), counts non-rejected bookings, inserts the
   order + items only if capacity remains, then `COMMIT`s. Two students racing for
   the last slot cannot both win.
5. **Response:** servlet redirects (Post/Redirect/Get) to `/track?id=...`, which
   forwards to `track.jsp` showing the live status timeline.

Passwords: `PasswordUtil` stores `SHA-256(salt + password)` with a random
16-byte salt per user; login re-hashes and compares in constant time.

## Notes / limitations

- `DBUtil` uses `DriverManager` (one connection per call). Fine for a college
  project; a real deployment would use a pool like HikariCP.
- Payment is "Pay at Canteen" by design — `payment_mode` is fixed at order time.
- The cart lives in the HTTP session; it does not survive logout.
- Student accounts are restricted to `@acpce.ac.in` emails (see
  `AuthServlet.COLLEGE_DOMAIN`) and must verify the email with a 6-digit code
  before first login. Phone-number OTP is intentionally left for later.

## Email verification (SMTP)

Registration sends a 6-digit verification code. Configure these environment
variables (Railway: service → Variables) to send real emails:

| Variable    | Example                          |
|-------------|----------------------------------|
| `SMTP_HOST` | `smtp.gmail.com`                 |
| `SMTP_PORT` | `587` (default)                  |
| `SMTP_USER` | `you@gmail.com`                  |
| `SMTP_PASS` | Gmail **app password** (see below)|
| `SMTP_FROM` | `you@gmail.com` (default = user) |

**Gmail app password** (free, 2 minutes): Google Account → Security → turn on
2-Step Verification → search "App passwords" → create one for "Mail" → paste
the 16-letter code as `SMTP_PASS`. Never use your real Gmail password.

**Demo mode:** if `SMTP_HOST` is not set, no email is sent — the code is logged
to stdout (Railway deploy logs) and shown on the verify page inside a clearly
labelled "Demo mode" box, so registration still works end-to-end for the viva.

## Deploying live on Railway (free trial)

The repo ships with a `Dockerfile`, so Railway can build and run it with zero
config. You need: a Railway account (free trial = $5 credit, 30 days, no card)
and this repo pushed to GitHub.

1. **Push to GitHub** — `git init`, commit everything, push to a new repo.
2. **Railway → New Project → Deploy from GitHub repo** — pick the repo.
   Railway detects the `Dockerfile` and builds the WAR automatically.
3. **Add MySQL** — in the project canvas: **+ New → Database → MySQL**.
   Railway injects `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`,
   `MYSQLPASSWORD` into the app automatically. `DBUtil` reads these first and
   only falls back to `db.properties` locally, so no code change is needed.
4. **Create the tables** — connect to the Railway MySQL once (Railway gives a
   one-click connect command / web console) and run `schema.sql`.
5. **Set the admin credentials** — in Railway Variables add `ADMIN_EMAIL` and
   `ADMIN_PASSWORD`; on next boot the app provisions the admin account from them.
6. **Open the app** — Railway assigns a public `*.up.railway.app` URL and you can
   log in with the admin email you configured.

Notes:
- Railway assigns the HTTP port via the `$PORT` env var; the Dockerfile rewrites
  Tomcat's connector to it at startup.
- The trial credit comfortably covers a Tomcat + MySQL demo for weeks.
- For viva, this gives you a live link the examiner can open on their own phone.
