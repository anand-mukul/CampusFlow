package com.campusflow.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "campusflow_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "rootpass";

    private DatabaseConfig() {
    }

    private static String getProperty(String key, String defaultValue) {
        String val = System.getProperty(key);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        String host = getProperty("DB_HOST", DEFAULT_HOST);
        String port = getProperty("DB_PORT", DEFAULT_PORT);
        String db = getProperty("DB_NAME", DEFAULT_DB);
        String user = getProperty("DB_USER", DEFAULT_USER);
        String password = getProperty("DB_PASSWORD", DEFAULT_PASSWORD);

        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host, port, db);
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("DB connection test failed: " + e.getMessage());
            return false;
        }
    }
}
