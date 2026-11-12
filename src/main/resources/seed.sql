-- =============================================================================
-- CampusFlow Demo Data Seed Script (Phase 1)
-- =============================================================================
-- INSTRUCTIONS:
-- 1. Ensure `schema.sql` has been executed first to create the tables:
--      mysql -u root -p < src/main/resources/schema.sql
-- 2. Execute this script:
--      mysql -u root -p < src/main/resources/seed.sql
--    OR run the Java seeder:
--      mvn exec:java -Dexec.mainClass=com.campusflow.util.DatabaseSeeder
-- =============================================================================

USE campusflow_db;

-- -----------------------------------------------------------------------------
-- 1. Demo Users
-- Passwords:
--   admin  / admin123    (admin@campusflow.com - Administrator)
--   daulat / faculty123  (daulat@campusflow.com - Er. Daulat Ram)
--   kiran  / faculty123  (kiran@campusflow.com - Er. Kiranpreet Kaur)
--   mukul  / student123  (mukul@campusflow.com - MUKUL ANAND)
--   mehak  / student123  (mehak@campusflow.com - MEHAKPREET KAUR)
--   divya  / student123  (divya@campusflow.com - DIVYA SHARMA)
-- -----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, email, role, is_active) VALUES
('admin',  '$2a$10$2wJ8xqpzppSvKADPA2yjFuHIyIEIovG/UkzJNNWe.STBQ9.LV6V12', 'admin@campusflow.com',  'ADMIN',   TRUE),
('daulat', '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'daulat@campusflow.com', 'FACULTY', TRUE),
('kiran',  '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'kiran@campusflow.com',  'FACULTY', TRUE),
('mukul',  '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'mukul@campusflow.com',  'STUDENT', TRUE),
('mehak',  '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'mehak@campusflow.com',  'STUDENT', TRUE),
('divya',  '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'divya@campusflow.com',  'STUDENT', TRUE)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), email = VALUES(email), role = VALUES(role), updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 2. Demo Faculty Records
-- -----------------------------------------------------------------------------
INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC001', 'Er. Daulat', 'Ram', 'daulat@campusflow.com', '+91-98765-01001', 'Computer Science', 'Associate Professor'
FROM users WHERE username = 'daulat'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email), updated_at = CURRENT_TIMESTAMP;

INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC002', 'Er. Kiranpreet', 'Kaur', 'kiran@campusflow.com', '+91-98765-01002', 'Electronics', 'Assistant Professor'
FROM users WHERE username = 'kiran'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email), updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 3. Demo Student Records
-- -----------------------------------------------------------------------------
INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU001', 'Mukul', 'Anand', 'mukul@campusflow.com', '+91-98765-02001', 'Computer Science', 3, '2024-08-15'
FROM users WHERE username = 'mukul'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email), updated_at = CURRENT_TIMESTAMP;

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU002', 'Mehakpreet', 'Kaur', 'mehak@campusflow.com', '+91-98765-02002', 'Electronics', 5, '2023-08-15'
FROM users WHERE username = 'mehak'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email), updated_at = CURRENT_TIMESTAMP;

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU003', 'Divya', 'Sharma', 'divya@campusflow.com', '+91-98765-02003', 'Computer Science', 1, '2025-08-15'
FROM users WHERE username = 'divya'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email), updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 4. Demo Courses
-- -----------------------------------------------------------------------------
INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS101', 'Introduction to Programming', 'Fundamentals of programming using Java and OOP', 4, 'Computer Science', f.id, 60
FROM faculty f WHERE f.faculty_id = 'FAC001'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS201', 'Data Structures & Algorithms', 'Core data structures, trees, graphs, and algorithmic complexity', 4, 'Computer Science', f.id, 60
FROM faculty f WHERE f.faculty_id = 'FAC001'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'EC101', 'Circuit Theory', 'Basic electrical circuit analysis and Ohm/Kirchhoff laws', 3, 'Electronics', f.id, 60
FROM faculty f WHERE f.faculty_id = 'FAC002'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'EC201', 'Digital Electronics', 'Logic gates, Boolean algebra, flip-flops, and counters', 3, 'Electronics', f.id, 60
FROM faculty f WHERE f.faculty_id = 'FAC002'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 5. Demo Enrollments
-- -----------------------------------------------------------------------------
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A-'
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS201'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B+'
FROM students s, courses c WHERE s.student_id = 'STU002' AND c.course_code = 'EC101'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B'
FROM students s, courses c WHERE s.student_id = 'STU002' AND c.course_code = 'EC201'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', NULL
FROM students s, courses c WHERE s.student_id = 'STU003' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- -----------------------------------------------------------------------------
-- 6. Demo Attendance Records
-- -----------------------------------------------------------------------------
INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by)
SELECT s.id, c.id, '2025-11-01', 'PRESENT', (SELECT id FROM users WHERE username = 'daulat')
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'PRESENT';

INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by)
SELECT s.id, c.id, '2025-11-03', 'PRESENT', (SELECT id FROM users WHERE username = 'daulat')
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'PRESENT';

INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by)
SELECT s.id, c.id, '2025-11-05', 'ABSENT', (SELECT id FROM users WHERE username = 'daulat')
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'ABSENT';

INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by)
SELECT s.id, c.id, '2025-11-07', 'PRESENT', (SELECT id FROM users WHERE username = 'daulat')
FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101'
ON DUPLICATE KEY UPDATE status = 'PRESENT';
