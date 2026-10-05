package com.canteen.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place that hands out JDBC connections.
 *
 * How it works: db.properties is loaded once from the classpath
 * (src/main/resources -> WEB-INF/classes). Every call to getConnection()
 * opens a fresh connection via DriverManager.
 *
 * Why DriverManager and not a connection pool?
 * This is a college mini-project: a pool (e.g. HikariCP) adds a dependency
 * and configuration for little benefit at classroom traffic levels.
 * In production you WOULD use a pool, because opening a TCP connection
 * per request is expensive under load.
 */
public final class DBUtil {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Railway (or any cloud host) provides DB config via environment
        // variables. Local runs fall back to db.properties. Env wins so the
        // same WAR deploys unchanged in both places.
        String url = System.getenv("MYSQL_URL");
        String user = System.getenv("MYSQLUSER");
        String password = System.getenv("MYSQLPASSWORD");
        if (url == null) {
            String host = System.getenv("MYSQLHOST");
            if (host != null) {
                String port = System.getenv("MYSQLPORT");
                String db = System.getenv("MYSQLDATABASE");
                if (port == null) port = "3306";
                if (db == null) db = "canteen";
                url = "jdbc:mysql://" + host + ":" + port + "/" + db
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }
        if (url == null) {
            Properties p = new Properties();
            try (InputStream in = DBUtil.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException(
                            "db.properties not found on classpath (expected in src/main/resources)");
                }
                p.load(in);
            } catch (IOException e) {
                throw new ExceptionInInitializerError("Cannot load db.properties: " + e.getMessage());
            }
            url = p.getProperty("db.url");
            if (user == null) user = p.getProperty("db.user");
            if (password == null) password = p.getProperty("db.password");
        }
        URL = url;
        USER = user;
        PASSWORD = password;

        // Explicit driver registration. (Modern JDBC can auto-discover via
        // ServiceLoader, but being explicit is viva-safe and avoids surprises
        // on some containers.)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found. Is mysql-connector-j in the WAR's WEB-INF/lib?");
        }
    }

    private DBUtil() {
        // utility class - no instances
    }

    /** Opens a new JDBC connection. Caller MUST close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
