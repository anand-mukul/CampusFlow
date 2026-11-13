package com.campusflow.util;

import com.campusflow.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Enterprise Database Seeder for CampusFlow.
 * Seeds Indian university students, faculty, courses, enrollments, and
 * a full 30-day (1 month) comprehensive attendance dataset.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("=== CampusFlow Database Seeder ===");
        ensureDatabaseExists();
        try {
            ensureTablesCreated();
            try (Connection conn = DatabaseConfig.getConnection()) {
                seedUsers(conn);
                seedFaculty(conn);
                seedStudents(conn);
                seedCourses(conn);
                seedEnrollments(conn);
                seedOneMonthAttendance(conn);

                System.out.println("=== Seeding complete! ===");
                System.out.println("Demo Login Credentials:");
                System.out.println("  Admin:   admin    / admin123    (admin@campusflow.com)");
                System.out.println("  Faculty: daulat   / faculty123  (daulat@campusflow.com - Er. Daulat Ram, CSE)");
                System.out.println("  Faculty: kiran    / faculty123  (kiran@campusflow.com - Er. Kiranpreet Kaur, CSE)");
                System.out.println("  Faculty: gautam   / faculty123  (gautam@campusflow.com - Dr. Gautam Kumar, CSE)");
                System.out.println("  Faculty: priyanka / faculty123  (priyanka@campusflow.com - Dr. Priyanka Sharma, IT)");
                System.out.println("  Faculty: rajesh   / faculty123  (rajesh@campusflow.com - Dr. Rajesh Verma, ECE)");
                System.out.println("  Student: mukul    / student123  (mukul@campusflow.com - Mukul Anand, CSE)");
                System.out.println("  Student: divya    / student123  (divya@campusflow.com - Divya Sharma, CSE)");
                System.out.println("  Student: abhay    / student123  (abhay@campusflow.com - Abhay Patel, CSE)");
                System.out.println("  Student: gaurish  / student123  (gaurish@campusflow.com - Gaurish Gaur, CSE)");
                System.out.println("  Student: mehak    / student123  (mehak@campusflow.com - Mehakpreet Kaur, ECE)");
                System.out.println("  Student: rohan    / student123  (rohan@campusflow.com - Rohan Malhotra, Defaulter <75%)");
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
        String sql = "INSERT INTO users (username, password_hash, email, role, is_active) VALUES (?, ?, ?, ?, TRUE) " +
                     "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), email = VALUES(email), role = VALUES(role)";
        Object[][] users = new Object[][] {
            // Admin
            {"admin",    PasswordUtil.hashPassword("admin123"),   "admin@campusflow.com",    "ADMIN"},
            // Faculty (Indian Professors)
            {"daulat",   PasswordUtil.hashPassword("faculty123"), "daulat@campusflow.com",   "FACULTY"},
            {"kiran",    PasswordUtil.hashPassword("faculty123"), "kiran@campusflow.com",    "FACULTY"},
            {"gautam",   PasswordUtil.hashPassword("faculty123"), "gautam@campusflow.com",   "FACULTY"},
            {"priyanka", PasswordUtil.hashPassword("faculty123"), "priyanka@campusflow.com", "FACULTY"},
            {"rajesh",   PasswordUtil.hashPassword("faculty123"), "rajesh@campusflow.com",   "FACULTY"},
            // Students (Indian Engineering Students)
            {"mukul",    PasswordUtil.hashPassword("student123"), "mukul@campusflow.com",    "STUDENT"},
            {"mehak",    PasswordUtil.hashPassword("student123"), "mehak@campusflow.com",    "STUDENT"},
            {"divya",    PasswordUtil.hashPassword("student123"), "divya@campusflow.com",    "STUDENT"},
            {"abhay",    PasswordUtil.hashPassword("student123"), "abhay@campusflow.com",    "STUDENT"},
            {"gaurish",  PasswordUtil.hashPassword("student123"), "gaurish@campusflow.com",  "STUDENT"},
            {"simran",   PasswordUtil.hashPassword("student123"), "simran@campusflow.com",   "STUDENT"},
            {"arjun",    PasswordUtil.hashPassword("student123"), "arjun@campusflow.com",    "STUDENT"},
            {"priya",    PasswordUtil.hashPassword("student123"), "priya@campusflow.com",    "STUDENT"},
            {"rohan",    PasswordUtil.hashPassword("student123"), "rohan@campusflow.com",    "STUDENT"},
            {"ananya",   PasswordUtil.hashPassword("student123"), "ananya@campusflow.com",   "STUDENT"}
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
            System.out.println("  Users seeded (1 Admin, 5 Faculty, 10 Students).");
        }
    }

    private static void seedFaculty(Connection conn) throws SQLException {
        String sql = "INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation) " +
                     "SELECT u.id, ?, ?, ?, ?, ?, ?, ? FROM users u WHERE u.username = ? " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "email = VALUES(email), department = VALUES(department), designation = VALUES(designation)";
        Object[][] faculty = new Object[][] {
            {"FAC001", "Er. Daulat",     "Ram",    "daulat@campusflow.com",   "+91-98765-01001", "Computer Science",        "Associate Professor", "daulat"},
            {"FAC002", "Er. Kiranpreet", "Kaur",   "kiran@campusflow.com",    "+91-98765-01002", "Computer Science",        "Assistant Professor", "kiran"},
            {"FAC003", "Dr. Gautam",     "Kumar",   "gautam@campusflow.com",   "+91-98765-01003", "Computer Science",        "Professor & HOD",     "gautam"},
            {"FAC004", "Dr. Priyanka",   "Sharma",  "priyanka@campusflow.com", "+91-98765-01004", "Information Technology", "Associate Professor", "priyanka"},
            {"FAC005", "Dr. Rajesh",     "Verma",   "rajesh@campusflow.com",   "+91-98765-01005", "Electronics & Comm",     "Professor",           "rajesh"}
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
            System.out.println("  Faculty seeded (5 Indian Engineering Faculty).");
        }
    }

    private static void seedStudents(Connection conn) throws SQLException {
        String sql = "INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date) " +
                     "SELECT u.id, ?, ?, ?, ?, ?, ?, ?, ? FROM users u WHERE u.username = ? " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "email = VALUES(email), department = VALUES(department), semester = VALUES(semester)";
        Object[][] students = new Object[][] {
            {"STU001", "Mukul",        "Anand",    "mukul@campusflow.com",   "+91-98765-02001", "Computer Science",        5, "2024-08-01", "mukul"},
            {"STU002", "Mehakpreet",   "Kaur",     "mehak@campusflow.com",   "+91-98765-02002", "Electronics & Comm",     5, "2024-08-01", "mehak"},
            {"STU003", "Divya",        "Sharma",   "divya@campusflow.com",   "+91-98765-02003", "Computer Science",        3, "2025-08-01", "divya"},
            {"STU004", "Abhay",        "Patel",    "abhay@campusflow.com",   "+91-98765-02004", "Computer Science",        5, "2024-08-01", "abhay"},
            {"STU005", "Gaurish",      "Gaur",     "gaurish@campusflow.com", "+91-98765-02005", "Computer Science",        5, "2024-08-01", "gaurish"},
            {"STU006", "Simranjit",    "Singh",    "simran@campusflow.com",  "+91-98765-02006", "Information Technology", 3, "2025-08-01", "simran"},
            {"STU007", "Arjun",        "Verma",    "arjun@campusflow.com",   "+91-98765-02007", "Electronics & Comm",     5, "2024-08-01", "arjun"},
            {"STU008", "Priya",        "Sharma",   "priya@campusflow.com",   "+91-98765-02008", "Computer Science",        3, "2025-08-01", "priya"},
            {"STU009", "Rohan",        "Malhotra", "rohan@campusflow.com",   "+91-98765-02009", "Computer Science",        5, "2024-08-01", "rohan"},
            {"STU010", "Ananya",       "Gupta",    "ananya@campusflow.com",  "+91-98765-02010", "Information Technology", 3, "2025-08-01", "ananya"}
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
            System.out.println("  Students seeded (10 Indian University Students).");
        }
    }

    private static void seedCourses(Connection conn) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students) " +
                     "SELECT ?, ?, ?, ?, ?, f.id, ? FROM faculty f WHERE f.faculty_id = ? " +
                     "ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), description = VALUES(description), " +
                     "credits = VALUES(credits), department = VALUES(department), faculty_id = VALUES(faculty_id), max_students = VALUES(max_students)";
        Object[][] courses = new Object[][] {
            {"CS101", "Object-Oriented Programming with Java", "Core Java OOP concepts, collections framework, Swing, and exception handling", 4, "Computer Science",        60, "FAC001"},
            {"CS201", "Data Structures & Algorithms",          "Linear & non-linear structures, balanced trees, graph theory, sorting, and complexity", 4, "Computer Science", 60, "FAC001"},
            {"CS301", "Database Management Systems",           "Relational algebra, SQL, normalization, concurrency control, and indexing strategies", 4, "Computer Science",  60, "FAC001"},
            {"CS302", "Operating Systems",                    "Process scheduling, virtual memory management, deadlock prevention, and POSIX threads", 4, "Computer Science", 60, "FAC002"},
            {"CS303", "Computer Networks",                     "OSI/TCP-IP stacks, routing protocols, flow control, socket programming, and TCP/UDP", 3, "Computer Science",   60, "FAC002"},
            {"AI301", "Artificial Intelligence & ML",          "Search heuristics, regression, classification, clustering, neural networks, and NLP", 4, "Computer Science",   50, "FAC003"},
            {"IT301", "Web Technologies & Cloud Computing",    "Full-stack RESTful architectures, cloud virtualization, containerization, and microservices", 3, "Information Technology", 50, "FAC004"},
            {"EC101", "Network Analysis & Synthesis",          "Two-port networks, Laplace transforms, AC circuit theorems, and frequency filters", 3, "Electronics & Comm",    45, "FAC005"},
            {"EC201", "Digital Electronics & Logic Design",    "Boolean algebra, combinational & sequential circuits, flip-flops, and FPGA design", 4, "Electronics & Comm",    45, "FAC005"}
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
            System.out.println("  Courses seeded (9 Indian University Core Courses).");
        }
    }

    private static void seedEnrollments(Connection conn) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade) " +
                     "SELECT s.id, c.id, '2025-08-15', 'ACTIVE', ? " +
                     "FROM students s, courses c WHERE s.student_id = ? AND c.course_code = ? " +
                     "ON DUPLICATE KEY UPDATE grade = VALUES(grade), status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Mukul Anand (STU001) - 4 courses
            addEnrollment(ps, "A+", "STU001", "CS101");
            addEnrollment(ps, "A",  "STU001", "CS201");
            addEnrollment(ps, "A-", "STU001", "CS301");
            addEnrollment(ps, "B+", "STU001", "CS302");

            // Divya Sharma (STU003) - 3 courses
            addEnrollment(ps, "A",  "STU003", "CS101");
            addEnrollment(ps, "A",  "STU003", "CS201");
            addEnrollment(ps, "A-", "STU003", "CS301");

            // Abhay Patel (STU004) - 4 courses
            addEnrollment(ps, "A",  "STU004", "CS101");
            addEnrollment(ps, "B+", "STU004", "CS201");
            addEnrollment(ps, "A-", "STU004", "CS301");
            addEnrollment(ps, "A",  "STU004", "AI301");

            // Gaurish Gaur (STU005) - 3 courses
            addEnrollment(ps, "B+", "STU005", "CS101");
            addEnrollment(ps, "A-", "STU005", "CS201");
            addEnrollment(ps, "A",  "STU005", "CS302");

            // Mehakpreet Kaur (STU002) - 3 courses
            addEnrollment(ps, "A",  "STU002", "EC101");
            addEnrollment(ps, "A+", "STU002", "EC201");
            addEnrollment(ps, "B+", "STU002", "CS101");

            // Arjun Verma (STU007) - 2 courses
            addEnrollment(ps, "B",  "STU007", "EC101");
            addEnrollment(ps, "B+", "STU007", "EC201");

            // Simranjit Singh (STU006) - 2 courses
            addEnrollment(ps, "A-", "STU006", "IT301");
            addEnrollment(ps, "B+", "STU006", "CS101");

            // Priya Sharma (STU008) - 3 courses
            addEnrollment(ps, "A",  "STU008", "CS101");
            addEnrollment(ps, "B+", "STU008", "CS201");
            addEnrollment(ps, "A-", "STU008", "IT301");

            // Rohan Malhotra (STU009 - Defaulter Demonstration) - 2 courses
            addEnrollment(ps, "C+", "STU009", "CS101");
            addEnrollment(ps, "C",  "STU009", "CS201");

            // Ananya Gupta (STU010) - 2 courses
            addEnrollment(ps, "A",  "STU010", "IT301");
            addEnrollment(ps, "A-", "STU010", "CS101");

            ps.executeBatch();
            System.out.println("  Enrollments seeded (28 Active Course Enrollments).");
        }
    }

    private static void addEnrollment(PreparedStatement ps, String grade, String studentId, String courseCode) throws SQLException {
        ps.setString(1, grade);
        ps.setString(2, studentId);
        ps.setString(3, courseCode);
        ps.addBatch();
    }

    /**
     * Seeds 1 full month of attendance logs across all enrolled courses and students.
     * Spans 20 distinct academic days (Monday-Friday) from 2026-10-14 to 2026-11-13.
     */
    private static void seedOneMonthAttendance(Connection conn) throws SQLException {
        // 20 academic weekdays over the past 30 days
        List<LocalDate> academicDays = List.of(
            LocalDate.of(2026, 10, 14),
            LocalDate.of(2026, 10, 15),
            LocalDate.of(2026, 10, 16),
            LocalDate.of(2026, 10, 19),
            LocalDate.of(2026, 10, 20),
            LocalDate.of(2026, 10, 21),
            LocalDate.of(2026, 10, 22),
            LocalDate.of(2026, 10, 23),
            LocalDate.of(2026, 10, 26),
            LocalDate.of(2026, 10, 27),
            LocalDate.of(2026, 10, 28),
            LocalDate.of(2026, 10, 29),
            LocalDate.of(2026, 10, 30),
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 3),
            LocalDate.of(2026, 11, 4),
            LocalDate.of(2026, 11, 5),
            LocalDate.of(2026, 11, 6),
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 10),
            LocalDate.of(2026, 11, 11),
            LocalDate.of(2026, 11, 12),
            LocalDate.of(2026, 11, 13)
        );

        String insertSql = "INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by) " +
                           "VALUES (?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE status = VALUES(status)";

        // Map of student_id -> roll code
        // Fetch all active enrollments from DB with instructor id
        String query = "SELECT e.student_id, e.course_id, s.student_id AS roll, c.course_code, " +
                       "       COALESCE(f.user_id, 1) AS instructor_user_id " +
                       "FROM enrollments e " +
                       "JOIN students s ON s.id = e.student_id " +
                       "JOIN courses c ON c.id = e.course_id " +
                       "LEFT JOIN faculty f ON f.id = c.faculty_id " +
                       "WHERE e.status = 'ACTIVE'";

        int inserted = 0;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            List<EnrollmentRecord> enrollments = new ArrayList<>();
            while (rs.next()) {
                enrollments.add(new EnrollmentRecord(
                    rs.getInt("student_id"),
                    rs.getInt("course_id"),
                    rs.getString("roll"),
                    rs.getString("course_code"),
                    rs.getInt("instructor_user_id")
                ));
            }

            for (EnrollmentRecord er : enrollments) {
                for (int d = 0; d < academicDays.size(); d++) {
                    LocalDate date = academicDays.get(d);
                    String status = computeRealisticStatus(er.roll, d);

                    ps.setInt(1, er.studentId);
                    ps.setInt(2, er.courseId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setString(4, status);
                    ps.setInt(5, er.instructorUserId);
                    ps.addBatch();
                    inserted++;
                }
            }
            ps.executeBatch();
            System.out.println("  Attendance seeded: " + inserted + " records across 1 month (23 academic days).");
        }
    }

    private static String computeRealisticStatus(String roll, int dayIndex) {
        // Rohan (STU009) is the intentional Defaulter (~65% attendance for demonstration)
        if ("STU009".equals(roll)) {
            if (dayIndex % 3 == 0) return "ABSENT";
            if (dayIndex == 5 || dayIndex == 14) return "ABSENT";
            return "PRESENT";
        }
        // Mukul (STU001): High performer (~91% attendance)
        if ("STU001".equals(roll)) {
            if (dayIndex == 7) return "LEAVE";
            if (dayIndex == 15) return "ABSENT";
            return "PRESENT";
        }
        // Divya (STU003): Top attendance (~96%)
        if ("STU003".equals(roll)) {
            if (dayIndex == 11) return "LEAVE";
            return "PRESENT";
        }
        // General realistic distribution for other students
        int mod = (roll.hashCode() + dayIndex) % 11;
        if (mod == 0) return "ABSENT";
        if (mod == 5) return "LEAVE";
        return "PRESENT";
    }

    private static class EnrollmentRecord {
        final int studentId;
        final int courseId;
        final String roll;
        final String courseCode;
        final int instructorUserId;

        EnrollmentRecord(int studentId, int courseId, String roll, String courseCode, int instructorUserId) {
            this.studentId = studentId;
            this.courseId = courseId;
            this.roll = roll;
            this.courseCode = courseCode;
            this.instructorUserId = instructorUserId;
        }
    }
}
