# Viva Notes — Campus Canteen Pre-Order & Pickup System

Short answers to 25 likely viva questions. Each maps to something real in the code.

## 1. What does your project do?
Students register/login, browse the canteen menu with search and veg/non-veg
filters, add items to a session cart, pick a pickup date + time slot, and place
an order (Pay at Canteen). They track live status and rate completed orders.
Admins manage the menu, accept/advance/reject orders, manage pickup slots, and
see dashboard stats and feedback.

## 2. How is MVC applied here?
**Model** = `com.canteen.model` POJOs + `com.canteen.dao` JDBC classes (data +
data access). **View** = JSPs under `WEB-INF/jsp` (Bootstrap UI, JSTL/EL, no Java
logic). **Controller** = servlets (`com.canteen.servlet`) that read request
parameters, call DAOs, and forward to a JSP or redirect.

## 3. Why Servlets/JSP instead of Spring?
The subject (FSJP) teaches the fundamentals: HTTP handling, sessions, JDBC.
A framework would hide exactly what the viva tests — request lifecycle, session
management, and SQL. Servlets make every step visible and explainable.

## 4. What is a Servlet, briefly? Its lifecycle?
A Java class that handles HTTP requests inside a servlet container (Tomcat).
Lifecycle: container loads it once → `init()` → per request a thread calls
`service()` → `doGet()`/`doPost()` → `destroy()` on shutdown. One instance
serves many threads, so servlets must be thread-safe (ours keep no request
state in fields — DAO instances are stateless).

## 5. Why map servlets with @WebServlet?
It's the annotation-based alternative to `<servlet-mapping>` in web.xml
(Servlet 3.0+). Less XML, mapping lives next to the code. We still use web.xml
for *filters* because filter execution order is only explicit in XML.

## 6. What is JDBC? Name its core steps.
Java Database Connectivity — the standard API for talking to relational DBs.
Steps: load driver → `DriverManager.getConnection(url, user, pass)` →
create `PreparedStatement` → set parameters → `executeQuery()`/`executeUpdate()`
→ process `ResultSet` → close everything (we use try-with-resources).

## 7. PreparedStatement vs Statement?
`Statement` takes a complete SQL string — concatenating user input into it
allows SQL injection. `PreparedStatement` uses `?` placeholders; the DB compiles
the SQL once and treats parameters as pure data, never as code. ALL our DAO
queries use PreparedStatement.

## 8. Give an example of SQL injection your code prevents.
Login: `SELECT ... WHERE email = ?` with the email as a parameter. With string
concatenation, an input like `' OR '1'='1` would log in without a password. With
a placeholder, that input is just compared as a literal string and matches
nothing.

## 9. Session vs cookies?
Cookies live on the client (small, visible, tamperable). The HTTP session lives
on the server; the client only holds a session id (usually in a cookie). We
store the logged-in `User` object and the cart `Map` in the session, so the
browser never sees or can forge them. Session timeout is 30 min (web.xml).

## 10. How does login work end-to-end?
`AuthServlet.doPost(/login)` → `UserDAO.findByEmail` → `PasswordUtil.verify`
re-hashes the typed password with the stored salt and compares → on success a
scrubbed `User` (hash/salt nulled) is put in session as `"user"` → redirect to
`next` or `/menu`. `AuthFilter` guards private URLs by checking that attribute.

## 11. How are passwords stored? Why a salt?
We store `(salt, SHA-256(salt + password))`, never plaintext. The random 16-byte
salt per user defeats rainbow tables: identical passwords produce different
hashes. (Production systems prefer bcrypt/Argon2, which are deliberately slow;
SHA-256+salt is the accepted college-level answer.)

## 12. What do your two filters do, and why that order?
`AuthFilter`: blocks unauthenticated access to `/cart`, `/checkout`, `/orders`,
`/track`, `/feedback`, `/slots`, `/admin/*` (redirects to login with `?next=`).
`AdminFilter`: on `/admin/*` additionally requires `role == ADMIN`, else a
friendly 403 page. Order matters — declared in web.xml: auth first, then role
check.

## 13. What is the order status lifecycle?
`PLACED → ACCEPTED → PREPARING → READY → PICKED_UP`, with `REJECTED` (plus a
reason) reachable from `PLACED`/`ACCEPTED`. `Order.allowedNext()` defines legal
transitions; both the admin servlet and `OrderDAO.updateStatus` enforce them, so
a crafted POST can't jump `PLACED → PICKED_UP`.

## 14. How is pickup-slot capacity enforced against double booking?
`OrderDAO.placeOrder()` runs in a single transaction: `SELECT ... FOR UPDATE`
locks the slot row, then it counts non-rejected orders for that slot, and only
inserts when `count < max_orders`, then commits. The row lock serialises two
simultaneous checkouts — the second waits, re-counts after the first commits,
and gets "slot is full". Check + insert are atomic, so overselling is
impossible.

## 15. Why recompute the order total on the server?
The browser only sends item ids and quantities. If we trusted a total from the
form, anyone could edit it (devtools) and pay less. The servlet totals
`price × qty` from the session cart, and the DAO freezes `price_at_order` per
line so later menu price changes never rewrite history.

## 16. Why DriverManager instead of a connection pool?
Simplicity for a mini project: one dependency fewer, no pool tuning, and the
code path (open → use → close in try-with-resources) is easy to explain. Trade-
off: opening a TCP connection per request is slow under load — a real system
would use HikariCP. This is documented in `DBUtil`.

## 17. What is a WAR file? What's inside ours?
Web ARchive — the deployable unit for servlet containers. Ours
(`target/campus-canteen.war`) contains: compiled classes (`WEB-INF/classes`),
`db.properties`, JSPs, `web.xml`, `WEB-INF/lib` (MySQL driver, JSTL jars), and
static files. Drop it in Tomcat's `webapps/` and Tomcat explodes and serves it.

## 18. What does `mvn package` do?
Runs the Maven lifecycle up to `package`: `validate → compile → test →
package`. It resolves dependencies from Maven Central, compiles
`src/main/java`, copies resources, and the `maven-war-plugin` assembles the
WAR. `servlet-api` is `provided` scope, so Tomcat's own copy is used instead of
bundling ours (bundling it would cause class conflicts).

## 19. How does the cart work without a database table?
It's a `LinkedHashMap<Integer, CartItem>` in the HTTP session (`"cart"`
attribute). Add/update/remove mutate the map; checkout reads it once and clears
it after the order commits. No DB table needed because a cart is temporary by
nature — it dies with the session/logout.

## 20. Client-side vs server-side validation — why both?
JavaScript validation (login/register/checkout forms) gives instant feedback
and cuts junk requests. Server-side validation (in servlets/DAOs) is the real
security — JS can be bypassed with curl/devtools, so the server re-checks
email format, password length, qty ranges, slot existence, and ownership of
every order id.

## 21. How do you avoid showing stack traces to users?
Every servlet catch block forwards to `/WEB-INF/jsp/error.jsp` with a friendly
`errorTitle`/`errorMessage`. web.xml additionally maps 404, 500, and
`java.lang.Exception` to the same page. Logs (not shown) keep the real trace
for the developer.

## 22. What is JSTL/EL and why use it in JSPs?
JSTL (JSP Standard Tag Library) + EL (Expression Language, `${...}`) replace
Java scriptlets in pages: `<c:forEach>`, `<c:if>`, `<fmt:formatNumber>` keep
JSPs as pure presentation. Our `header.jsp` declares the `jakarta.tags.core`
taglibs once and every page static-includes it.

## 23. How does the checkout page load slots without a page reload?
Changing the date pills fires `fetch('/slots?date=...')`. `SlotServlet`
returns JSON `[{id, label, remaining}]` (hand-built, no library) and the JS
rebuilds the radio list, disabling full slots. The same data is server-rendered
on first load, so the page works even with JS off.

## 24. How is feedback restricted to completed orders?
`feedback.jsp` is only linked for `PICKED_UP` orders without existing feedback.
`FeedbackDAO.add` re-checks in SQL: the order must belong to the user AND be
`PICKED_UP`; the `UNIQUE(order_id)` constraint rejects a second rating for the
same order even if someone double-posts.

## 25. If the examiner asks "what would you improve?", what do you say?
Three honest answers: (1) connection pooling (HikariCP) instead of
DriverManager; (2) password hashing with bcrypt instead of SHA-256; (3) an
order-confirmation SMS/email hook and a real payment gateway interface. And the
admin password must be changed from the seeded default — noted in the README.

## 26. Why did the cart page crash with a 500 error once, and how was it fixed?
`${it.lineTotal}` in `cart.jsp` is EL property access, which resolves to a
JavaBeans getter (`getLineTotal()`). `CartItem` only had a plain method
`lineTotal()`, so EL threw `PropertyNotFoundException` mid-render. Fix: added
a proper `getLineTotal()` getter delegating to `lineTotal()`. Lesson: in JSP
EL, `${x.y}` always means `getY()`, never an arbitrary method.

## 27. How is registration restricted to college emails?
`AuthServlet.COLLEGE_DOMAIN = "acpce.ac.in"`. `validate()` rejects any other
domain server-side (the JSP also checks in JS first), and `doLogin` re-checks
for students — the seeded admin account is exempt. Both checks are
case-insensitive via `toLowerCase()`.

## 28. Walk me through the email verification flow.
Register → row created with `email_verified = FALSE` → `SecureRandom`
6-digit code stored with a 15-minute expiry (`verify_code`,
`verify_expires`) → sent via SMTP (`EmailUtil`, Jakarta Mail) → user lands on
`/verify`. Correct code (compared with `MessageDigest.isEqual`, timing-safe)
→ `markEmailVerified()` sets the flag and clears the code → session login →
`/menu`. Login before verifying redirects back to `/verify`. Without
`SMTP_HOST` set, the app runs in labelled demo mode: the code is logged and
shown on the verify page so the flow still works end-to-end.

## 29. What happens to the database when this new version deploys over the old one?
`SchemaInitListener` now runs a migration when the `users` table already
exists: `ALTER TABLE ... ADD COLUMN` for `email_verified`, `verify_code`,
`verify_expires`, swallowing MySQL error 1060 (duplicate column) so re-deploys
are idempotent. Existing admins are backfilled to verified.
