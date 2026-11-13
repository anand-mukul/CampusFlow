package com.campusflow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "campusflow_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "rootpass";

    private static final Properties externalProperties = new Properties();
    private static boolean propertiesLoaded = false;
    private static String configSourceDescription = "Defaults";

    private DatabaseConfig() {}

    /**
     * Attempts to load external properties from multiple prioritized locations:
     * 1. Path specified via -Dconfig.file=<path> or -Dapp.config=<path>
     * 2. ./config/application.properties
     * 3. ./application.properties
     */
    private static synchronized void ensurePropertiesLoaded() {
        if (propertiesLoaded) return;
        propertiesLoaded = true;

        String customPath = System.getProperty("config.file", System.getProperty("app.config"));
        File configFile = null;

        if (customPath != null && !customPath.isBlank()) {
            configFile = new File(customPath.trim());
        }

        if (configFile == null || !configFile.exists()) {
            File localConfigDir = new File("config" + File.separator + "application.properties");
            if (localConfigDir.exists()) {
                configFile = localConfigDir;
            }
        }

        if (configFile == null || !configFile.exists()) {
            File rootFile = new File("application.properties");
            if (rootFile.exists()) {
                configFile = rootFile;
            }
        }

        if (configFile != null && configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                externalProperties.load(in);
                configSourceDescription = "External File (" + configFile.getAbsolutePath() + ")";
                System.out.println("[DatabaseConfig] Loaded configuration from: " + configSourceDescription);
            } catch (Exception e) {
                System.err.println("[DatabaseConfig] Warning: Failed to load external config: " + e.getMessage());
            }
        } else {
            configSourceDescription = "System Properties / Environment / Built-in Defaults";
        }
    }

    private static String getProperty(String key, String altKey, String defaultValue) {
        ensurePropertiesLoaded();

        // 1. Check System Properties (highest precedence)
        String val = System.getProperty(key);
        if (val != null && !val.isBlank()) return val.trim();

        if (altKey != null) {
            val = System.getProperty(altKey);
            if (val != null && !val.isBlank()) return val.trim();
        }

        // 2. Check Environment Variables
        if (altKey != null) {
            val = System.getenv(altKey);
            if (val != null && !val.isBlank()) return val.trim();
        }
        val = System.getenv(key.replace('.', '_').toUpperCase());
        if (val != null && !val.isBlank()) return val.trim();

        // 3. Check External Properties File
        val = externalProperties.getProperty(key);
        if (val != null && !val.isBlank()) return val.trim();

        if (altKey != null) {
            val = externalProperties.getProperty(altKey);
            if (val != null && !val.isBlank()) return val.trim();
        }

        // 4. Default fallback
        return defaultValue;
    }

    public static String getHost() { return getProperty("db.host", "DB_HOST", DEFAULT_HOST); }
    public static String getPort() { return getProperty("db.port", "DB_PORT", DEFAULT_PORT); }
    public static String getDatabaseName() { return getProperty("db.name", "DB_NAME", DEFAULT_DB); }
    public static String getUser() { return getProperty("db.user", "DB_USER", DEFAULT_USER); }
    public static String getPassword() { return getProperty("db.password", "DB_PASSWORD", DEFAULT_PASSWORD); }
    public static String getConfigSource() { ensurePropertiesLoaded(); return configSourceDescription; }

    public static Connection getConnection() throws SQLException {
        String host = getHost();
        String port = getPort();
        String db = getDatabaseName();
        String user = getUser();
        String password = getPassword();

        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=5000",
                host, port, db);
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String sanitizedMsg = String.format(
                    "Unable to connect to MySQL database '%s' at %s:%s as user '%s'. Cause: %s",
                    db, host, port, user, e.getMessage());
            throw new SQLException(sanitizedMsg, e.getSQLState(), e.getErrorCode());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
