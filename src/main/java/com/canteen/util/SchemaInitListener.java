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
                return;
            }
            LOG.info("SchemaInit: empty database detected, creating schema...");
            runScript(c);
            LOG.info("SchemaInit: done. Default admin: admin@canteen.local / admin123 (CHANGE IT).");
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
            "UPDATE users SET email_verified = TRUE WHERE role = 'ADMIN'"
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
