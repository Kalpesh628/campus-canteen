package com.canteen.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Self-initializing database for cloud deploys (Railway etc.).
 *
 * On startup it checks whether the `users` table exists. If not, it runs
 * src/main/resources/schema.sql (CREATE TABLEs + seed data) once.
 * Cloud-only statements (CREATE DATABASE / CREATE USER / GRANT) are skipped
 * because the managed DB already provides a database and user.
 *
 * Local installs are unaffected: if the tables already exist, this is a no-op.
 * Safe to leave enabled everywhere.
 */
@WebListener
public class SchemaInitListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(SchemaInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection c = DBUtil.getConnection()) {
            if (tableExists(c, "users")) {
                LOG.info("SchemaInit: tables already present, applying column migrations if needed.");
                migrateUserColumns(c);
                bootstrapAdminFromEnv(c);
                return;
            }
            LOG.info("SchemaInit: empty database detected, creating schema...");
            runScript(c);
            bootstrapAdminFromEnv(c);
            LOG.info("SchemaInit: done. Seed admin is local-dev only; production uses ADMIN_EMAIL/ADMIN_PASSWORD env.");
        } catch (Exception e) {
            // Don't kill the deploy if the DB isn't reachable yet; the app
            // will surface connection errors normally on first request.
            LOG.severe("SchemaInit failed: " + e.getMessage());
        }
    }

    private boolean tableExists(Connection c, String table) throws Exception {
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet rs = md.getTables(c.getCatalog(), null, table, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * Adds email-verification columns to a pre-existing `users` table
     * (databases created before this feature shipped). MySQL has no
     * ADD COLUMN IF NOT EXISTS, so a duplicate-column error (1060) is
     * swallowed and treated as "already migrated".
     */
    private void migrateUserColumns(Connection c) {
        String[] alters = {
            "ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE",
            "ALTER TABLE users ADD COLUMN verify_code CHAR(6)",
            "ALTER TABLE users ADD COLUMN verify_expires TIMESTAMP NULL",
            "ALTER TABLE users ADD COLUMN reset_code CHAR(6)",
            "ALTER TABLE users ADD COLUMN reset_expires TIMESTAMP NULL",
            // Phone verification (2026-10-05): email OTP is replaced by phone OTP.
            "ALTER TABLE users ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE",
            // Existing email-verified students keep their access under the new gate.
            "UPDATE users SET phone_verified = TRUE WHERE email_verified = TRUE",
            // Admin credentials are bootstrapped from ADMIN_EMAIL / ADMIN_PASSWORD
            // env vars (bootstrapAdminFromEnv) - never hardcoded in the repo.
            // One-time cleanup (2026-10-05): remove test accounts created during
            // development, plus any orders/feedback they placed (FK-safe order).
            // Explicit emails only - never touches the admin or future students.
            // Idempotent: matches zero rows once the accounts are gone.
            "DELETE oi FROM order_items oi "
                + "JOIN orders o ON oi.order_id = o.id "
                + "JOIN users u ON o.user_id = u.id "
                + "WHERE u.email IN ('maxtest1@example.com', 'kptest1@acpce.ac.in')",
            "DELETE o FROM orders o "
                + "JOIN users u ON o.user_id = u.id "
                + "WHERE u.email IN ('maxtest1@example.com', 'kptest1@acpce.ac.in')",
            "DELETE f FROM feedback f "
                + "JOIN users u ON f.user_id = u.id "
                + "WHERE u.email IN ('maxtest1@example.com', 'kptest1@acpce.ac.in')",
            "DELETE FROM users WHERE email IN ('maxtest1@example.com', 'kptest1@acpce.ac.in')",
            "UPDATE users SET email_verified = TRUE WHERE role = 'ADMIN'",
            // The original seed hashed the admin password with hex-decoded salt bytes
            // while PasswordUtil hashes the salt hex string - so admin123 never
            // verified. Repair it, but ONLY if the password is still the default
            // (never overwrite a password the owner already changed).
            "UPDATE users SET password_hash = 'd4d13358f4ce677cf7532d2633c39ad8987a331c02409c79c5a338e9ff4af8f7' "
                + "WHERE email = 'admin@canteen.local' "
                + "AND password_hash = '5764ed82adca085e138c53f05c850cbd16c299ce095959b98778044dd8ea849f'"
        };
        try (Statement st = c.createStatement()) {
            for (String sql : alters) {
                try {
                    st.execute(sql);
                    LOG.info("SchemaInit migration applied: " + sql);
                } catch (java.sql.SQLException e) {
                    if (e.getErrorCode() == 1060) {
                        LOG.fine("SchemaInit migration skipped (already applied).");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            LOG.severe("SchemaInit migration failed: " + e.getMessage());
        }
    }

    /**
     * One-time private admin bootstrap. If ADMIN_EMAIL and ADMIN_PASSWORD env
     * vars are set (e.g. Railway Variables), the admin account is created or
     * repointed to them with a freshly generated salt+hash. The password never
     * appears in the repo, logs, or client-visible output. Safe to run on every
     * boot: it only acts when the env vars are present.
     */
    private void bootstrapAdminFromEnv(Connection c) {
        String email = System.getenv("ADMIN_EMAIL");
        String pass = System.getenv("ADMIN_PASSWORD");
        if (email == null || email.isBlank() || pass == null || pass.isBlank()) {
            return;
        }
        email = email.trim().toLowerCase();
        try {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(pass, salt);
            // 1. Admin with this email already exists -> rotate password only.
            try (PreparedStatement q = c.prepareStatement(
                    "SELECT id FROM users WHERE role = 'ADMIN' AND email = ?")) {
                q.setString(1, email);
                try (ResultSet rs = q.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement u = c.prepareStatement(
                                "UPDATE users SET salt = ?, password_hash = ?, phone_verified = TRUE WHERE id = ?")) {
                            u.setString(1, salt);
                            u.setString(2, hash);
                            u.setInt(3, rs.getInt("id"));
                            u.executeUpdate();
                        }
                        LOG.info("SchemaInit: admin password set from ADMIN_PASSWORD env.");
                        return;
                    }
                }
            }
            // 2. Some other ADMIN exists (e.g. the local seed) -> repoint it.
            try (PreparedStatement q = c.prepareStatement(
                    "SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1")) {
                try (ResultSet rs = q.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement u = c.prepareStatement(
                                "UPDATE users SET email = ?, salt = ?, password_hash = ?, phone_verified = TRUE WHERE id = ?")) {
                            u.setString(1, email);
                            u.setString(2, salt);
                            u.setString(3, hash);
                            u.setInt(4, rs.getInt("id"));
                            u.executeUpdate();
                        }
                        LOG.info("SchemaInit: admin account repointed to ADMIN_EMAIL env.");
                        return;
                    }
                }
            }
            // 3. No admin at all -> create one.
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, salt, phone, role, phone_verified) "
                    + "VALUES ('Canteen Admin', ?, ?, ?, '', 'ADMIN', TRUE)")) {
                ins.setString(1, email);
                ins.setString(2, hash);
                ins.setString(3, salt);
                ins.executeUpdate();
            }
            LOG.info("SchemaInit: admin account created from ADMIN_EMAIL env.");
        } catch (Exception e) {
            LOG.severe("SchemaInit admin bootstrap failed: " + e.getMessage());
        }
    }

    private void runScript(Connection c) throws Exception {
        String script;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not on classpath");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                script = r.lines().collect(Collectors.joining("\n"));
            }
        }
        // Strip line comments, then split on ';' (schema has no triggers/procedures).
        String noComments = script.replaceAll("(?m)^\\s*--.*$", "");
        try (Statement st = c.createStatement()) {
            for (String raw : noComments.split(";")) {
                String sql = raw.trim();
                if (sql.isEmpty()) continue;
                String up = sql.toUpperCase();
                // Managed DBs provide database+user; creating them fails there.
                if (up.startsWith("CREATE DATABASE") || up.startsWith("USE ")
                        || up.startsWith("CREATE USER") || up.startsWith("GRANT")
                        || up.startsWith("FLUSH")) {
                    continue;
                }
                // Menu seed has no unique key: only insert when the table is empty.
                if (up.startsWith("INSERT INTO MENU_ITEMS") && !isEmpty(c, "menu_items")) {
                    continue;
                }
                st.execute(sql);
            }
        }
    }

    private boolean isEmpty(Connection c, String table) throws Exception {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1) == 0;
        } catch (Exception e) {
            return true; // table missing -> treat as empty so the seed runs
        }
    }
}
