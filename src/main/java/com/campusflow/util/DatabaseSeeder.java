package com.campusflow.util;

import com.campusflow.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run this class once to initialize the schema and seed the database with demo data.
 * Usage: mvn exec:java -Dexec.mainClass=com.campusflow.util.DatabaseSeeder
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("=== CampusFlow Database Seeder ===");
        try {
            ensureDatabaseExists();
            ensureTablesCreated();

            try (Connection conn = DatabaseConfig.getConnection()) {
                System.out.println("Connected to database successfully.");
                seedUsers(conn);
                seedStudents(conn);
                seedFaculty(conn);
                seedCourses(conn);
                seedEnrollments(conn);
                seedAttendance(conn);
                System.out.println("=== Seeding complete! ===");
                System.out.println("Demo credentials:");
                System.out.println("  Admin:   admin    / admin123    (or admin@campusflow.com)");
                System.out.println("  Faculty: daulat   / faculty123  (or daulat@campusflow.com - Er. Daulat Ram)");
                System.out.println("  Faculty: kiran    / faculty123  (or kiran@campusflow.com - Er. Kiranpreet Kaur)");
                System.out.println("  Student: mukul    / student123  (or mukul@campusflow.com - MUKUL ANAND)");
                System.out.println("  Student: mehak    / student123  (or mehak@campusflow.com - MEHAKPREET KAUR)");
                System.out.println("  Student: divya    / student123  (or divya@campusflow.com - DIVYA SHARMA)");
            }
        } catch (SQLException e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureDatabaseExists() {
        String host = System.getProperty("DB_HOST", "localhost");
        String port = System.getProperty("DB_PORT", "3306");
        String user = System.getProperty("DB_USER", "root");
        String pass = System.getProperty("DB_PASSWORD", "rootpass");

        String url = String.format("jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port);
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS campusflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("  Database 'campusflow_db' verified.");
        } catch (SQLException e) {
            System.out.println("  Note: Database creation check: " + e.getMessage());
        }
    }

    private static void ensureTablesCreated() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream in = DatabaseSeeder.class.getResourceAsStream("/schema.sql");
            if (in == null) {
                in = DatabaseSeeder.class.getClassLoader().getResourceAsStream("schema.sql");
            }
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
                        sb.append(line).append(" ");
                        if (trimmed.endsWith(";")) {
                            String sql = sb.toString().trim();
                            // Remove trailing semicolon for executeUpdate
                            if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1);
                            if (!sql.toUpperCase().startsWith("CREATE DATABASE") && !sql.toUpperCase().startsWith("USE ")) {
                                stmt.execute(sql);
                            }
                            sb.setLength(0);
                        }
                    }
                    try {
                        stmt.executeUpdate("ALTER TABLE attendance MODIFY COLUMN status ENUM('PRESENT','ABSENT','LATE','LEAVE') NOT NULL");
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    System.err.println("Error reading schema.sql: " + e.getMessage());
                }
            }
            System.out.println("  Database schema tables verified.");
        }
    }

    private static void seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, role) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), email = VALUES(email), role = VALUES(role)";
        Object[][] users = new Object[][] {
            {"admin",  PasswordUtil.hashPassword("admin123"),   "admin@campusflow.com",  "ADMIN"},
            {"daulat", PasswordUtil.hashPassword("faculty123"), "daulat@campusflow.com", "FACULTY"},
            {"kiran",  PasswordUtil.hashPassword("faculty123"), "kiran@campusflow.com",  "FACULTY"},
            {"mukul",  PasswordUtil.hashPassword("student123"), "mukul@campusflow.com",  "STUDENT"},
            {"mehak",  PasswordUtil.hashPassword("student123"), "mehak@campusflow.com",  "STUDENT"},
            {"divya",  PasswordUtil.hashPassword("student123"), "divya@campusflow.com",  "STUDENT"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] u : users) {
                ps.setString(1, (String) u[0]);
                ps.setString(2, (String) u[1]);
                ps.setString(3, (String) u[2]);
                ps.setString(4, (String) u[3]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  Users seeded.");
        }
    }

    private static void seedStudents(Connection conn) throws SQLException {
        String sql = "INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date) " +
                     "SELECT u.id, ?, ?, ?, ?, ?, ?, ?, ? FROM users u WHERE u.username = ? " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "email = VALUES(email), department = VALUES(department), semester = VALUES(semester)";
        Object[][] students = new Object[][] {
            {"STU001", "Mukul",      "Anand",  "mukul@campusflow.com", "+91-98765-02001", "Computer Science", 3, "2024-08-15", "mukul"},
            {"STU002", "Mehakpreet", "Kaur",   "mehak@campusflow.com", "+91-98765-02002", "Electronics",      5, "2023-08-15", "mehak"},
            {"STU003", "Divya",      "Sharma", "divya@campusflow.com", "+91-98765-02003", "Computer Science", 1, "2025-08-15", "divya"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] s : students) {
                ps.setString(1, (String) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setString(5, (String) s[4]);
                ps.setString(6, (String) s[5]);
                ps.setInt(7, (int) s[6]);
                ps.setString(8, (String) s[7]);
                ps.setString(9, (String) s[8]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  Students seeded.");
        }
    }

    private static void seedFaculty(Connection conn) throws SQLException {
        String sql = "INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation) " +
                     "SELECT u.id, ?, ?, ?, ?, ?, ?, ? FROM users u WHERE u.username = ? " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "email = VALUES(email), department = VALUES(department), designation = VALUES(designation)";
        Object[][] faculty = new Object[][] {
            {"FAC001", "Er. Daulat",     "Ram",  "daulat@campusflow.com", "+91-98765-01001", "Computer Science", "Associate Professor", "daulat"},
            {"FAC002", "Er. Kiranpreet", "Kaur", "kiran@campusflow.com",  "+91-98765-01002", "Electronics",      "Assistant Professor", "kiran"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] f : faculty) {
                ps.setString(1, (String) f[0]);
                ps.setString(2, (String) f[1]);
                ps.setString(3, (String) f[2]);
                ps.setString(4, (String) f[3]);
                ps.setString(5, (String) f[4]);
                ps.setString(6, (String) f[5]);
                ps.setString(7, (String) f[6]);
                ps.setString(8, (String) f[7]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  Faculty seeded.");
        }
    }

    private static void seedCourses(Connection conn) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students) " +
                     "SELECT ?, ?, ?, ?, ?, f.id, ? FROM faculty f WHERE f.faculty_id = ? " +
                     "ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), description = VALUES(description), " +
                     "credits = VALUES(credits), department = VALUES(department), faculty_id = VALUES(faculty_id)";
        Object[][] courses = new Object[][] {
            {"CS101", "Introduction to Programming",  "Fundamentals of programming using Java and OOP", 4, "Computer Science", 60, "FAC001"},
            {"CS201", "Data Structures & Algorithms", "Core data structures, trees, graphs, and complexity", 4, "Computer Science", 60, "FAC001"},
            {"EC101", "Circuit Theory",               "Basic electrical circuit analysis and Ohm/Kirchhoff laws", 3, "Electronics", 60, "FAC002"},
            {"EC201", "Digital Electronics",          "Logic gates, Boolean algebra, flip-flops, and counters", 3, "Electronics", 60, "FAC002"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] c : courses) {
                ps.setString(1, (String) c[0]);
                ps.setString(2, (String) c[1]);
                ps.setString(3, (String) c[2]);
                ps.setInt(4, (int) c[3]);
                ps.setString(5, (String) c[4]);
                ps.setInt(6, (int) c[5]);
                ps.setString(7, (String) c[6]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  Courses seeded.");
        }
    }

    private static void seedEnrollments(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO enrollments (student_id, course_id, enrollment_date, status, grade) " +
                     "SELECT s.id, c.id, '2025-08-15', 'ACTIVE', ? " +
                     "FROM students s, courses c WHERE s.student_id = ? AND c.course_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            addEnrollment(ps, "A",  "STU001", "CS101");
            addEnrollment(ps, "A-", "STU001", "CS201");
            addEnrollment(ps, "B+", "STU002", "EC101");
            addEnrollment(ps, "B",  "STU002", "EC201");
            addEnrollment(ps, "A",  "STU003", "CS101");
            ps.executeBatch();
            System.out.println("  Enrollments seeded.");
        }
    }

    private static void addEnrollment(PreparedStatement ps, String grade, String studentId, String courseCode) throws SQLException {
        ps.setString(1, grade);
        ps.setString(2, studentId);
        ps.setString(3, courseCode);
        ps.addBatch();
    }

    private static void seedAttendance(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, marked_by) " +
                     "SELECT s.id, c.id, ?, ?, (SELECT id FROM users WHERE username='daulat') " +
                     "FROM students s, courses c WHERE s.student_id = ? AND c.course_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            addAttendance(ps, "2025-11-01", "PRESENT", "STU001", "CS101");
            addAttendance(ps, "2025-11-03", "PRESENT", "STU001", "CS101");
            addAttendance(ps, "2025-11-05", "ABSENT",  "STU001", "CS101");
            addAttendance(ps, "2025-11-07", "PRESENT", "STU001", "CS101");
            addAttendance(ps, "2025-11-01", "PRESENT", "STU001", "CS201");
            addAttendance(ps, "2025-11-03", "LATE",    "STU001", "CS201");
            addAttendance(ps, "2025-11-05", "PRESENT", "STU001", "CS201");
            addAttendance(ps, "2025-11-01", "PRESENT", "STU002", "EC101");
            addAttendance(ps, "2025-11-03", "PRESENT", "STU002", "EC101");
            addAttendance(ps, "2025-11-05", "PRESENT", "STU002", "EC101");
            addAttendance(ps, "2025-11-07", "ABSENT",  "STU002", "EC101");
            addAttendance(ps, "2025-11-01", "PRESENT", "STU003", "CS101");
            addAttendance(ps, "2025-11-03", "PRESENT", "STU003", "CS101");
            addAttendance(ps, "2025-11-05", "PRESENT", "STU003", "CS101");
            ps.executeBatch();
            System.out.println("  Attendance seeded.");
        }
    }

    private static void addAttendance(PreparedStatement ps, String date, String status, String studentId, String courseCode) throws SQLException {
        ps.setString(1, date);
        ps.setString(2, status);
        ps.setString(3, studentId);
        ps.setString(4, courseCode);
        ps.addBatch();
    }
}
