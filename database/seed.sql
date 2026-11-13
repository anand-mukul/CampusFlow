-- =============================================================================
-- CampusFlow Demo Data Seed Script (Indian University Edition)
-- =============================================================================
-- Credentials Summary:
--   Admin:   admin    / admin123   (Role: ADMIN)
--   Faculty: daulat   / faculty123 (Role: FACULTY - Er. Daulat Ram, CSE)
--   Faculty: kiran    / faculty123 (Role: FACULTY - Er. Kiranpreet Kaur, CSE)
--   Faculty: gautam   / faculty123 (Role: FACULTY - Dr. Gautam Kumar, CSE)
--   Faculty: priyanka / faculty123 (Role: FACULTY - Dr. Priyanka Sharma, IT)
--   Faculty: rajesh   / faculty123 (Role: FACULTY - Dr. Rajesh Verma, ECE)
--   Student: mukul    / student123 (Role: STUDENT - Mukul Anand, CSE)
--   Student: divya    / student123 (Role: STUDENT - Divya Sharma, CSE)
--   Student: abhay    / student123 (Role: STUDENT - Abhay Patel, CSE)
--   Student: gaurish  / student123 (Role: STUDENT - Gaurish Gaur, CSE)
--   Student: mehak    / student123 (Role: STUDENT - Mehakpreet Kaur, ECE)
--   Student: rohan    / student123 (Role: STUDENT - Rohan Malhotra, Defaulter <75%)
-- =============================================================================

USE campusflow_db;

-- 1. Demo User Authentication Accounts
INSERT INTO users (username, password_hash, email, role, is_active) VALUES
('admin',    '$2a$10$2wJ8xqpzppSvKADPA2yjFuHIyIEIovG/UkzJNNWe.STBQ9.LV6V12', 'admin@campusflow.com',    'ADMIN',   TRUE),
('daulat',   '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'daulat@campusflow.com',   'FACULTY', TRUE),
('kiran',    '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'kiran@campusflow.com',    'FACULTY', TRUE),
('gautam',   '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'gautam@campusflow.com',   'FACULTY', TRUE),
('priyanka', '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'priyanka@campusflow.com', 'FACULTY', TRUE),
('rajesh',   '$2a$10$lkTFVh9wEFEW9g/.vw9AOuln1yWGvE4iwJofr1LdlZAIT5MI9TFAK', 'rajesh@campusflow.com',   'FACULTY', TRUE),
('mukul',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'mukul@campusflow.com',    'STUDENT', TRUE),
('mehak',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'mehak@campusflow.com',    'STUDENT', TRUE),
('divya',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'divya@campusflow.com',    'STUDENT', TRUE),
('abhay',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'abhay@campusflow.com',    'STUDENT', TRUE),
('gaurish',  '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'gaurish@campusflow.com',  'STUDENT', TRUE),
('simran',   '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'simran@campusflow.com',   'STUDENT', TRUE),
('arjun',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'arjun@campusflow.com',    'STUDENT', TRUE),
('priya',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'priya@campusflow.com',    'STUDENT', TRUE),
('rohan',    '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'rohan@campusflow.com',    'STUDENT', TRUE),
('ananya',   '$2a$10$Xkvnj.VnxmApm783kZp83.oUKymGkU5KYS6d.T7jZnfaNObHcspda', 'ananya@campusflow.com',   'STUDENT', TRUE)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), email = VALUES(email), role = VALUES(role), updated_at = CURRENT_TIMESTAMP;

-- 2. Demo Faculty Records
INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC001', 'Er. Daulat', 'Ram', 'daulat@campusflow.com', '+91-98765-01001', 'Computer Science', 'Associate Professor'
FROM users WHERE username = 'daulat'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC002', 'Er. Kiranpreet', 'Kaur', 'kiran@campusflow.com', '+91-98765-01002', 'Computer Science', 'Assistant Professor'
FROM users WHERE username = 'kiran'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC003', 'Dr. Gautam', 'Kumar', 'gautam@campusflow.com', '+91-98765-01003', 'Computer Science', 'Professor & HOD'
FROM users WHERE username = 'gautam'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC004', 'Dr. Priyanka', 'Sharma', 'priyanka@campusflow.com', '+91-98765-01004', 'Information Technology', 'Associate Professor'
FROM users WHERE username = 'priyanka'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation)
SELECT id, 'FAC005', 'Dr. Rajesh', 'Verma', 'rajesh@campusflow.com', '+91-98765-01005', 'Electronics & Comm', 'Professor'
FROM users WHERE username = 'rajesh'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

-- 3. Demo Student Records
INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU001', 'Mukul', 'Anand', 'mukul@campusflow.com', '+91-98765-02001', 'Computer Science', 5, '2024-08-01' FROM users WHERE username = 'mukul'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU002', 'Mehakpreet', 'Kaur', 'mehak@campusflow.com', '+91-98765-02002', 'Electronics & Comm', 5, '2024-08-01' FROM users WHERE username = 'mehak'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU003', 'Divya', 'Sharma', 'divya@campusflow.com', '+91-98765-02003', 'Computer Science', 3, '2025-08-01' FROM users WHERE username = 'divya'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU004', 'Abhay', 'Patel', 'abhay@campusflow.com', '+91-98765-02004', 'Computer Science', 5, '2024-08-01' FROM users WHERE username = 'abhay'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU005', 'Gaurish', 'Gaur', 'gaurish@campusflow.com', '+91-98765-02005', 'Computer Science', 5, '2024-08-01' FROM users WHERE username = 'gaurish'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU006', 'Simranjit', 'Singh', 'simran@campusflow.com', '+91-98765-02006', 'Information Technology', 3, '2025-08-01' FROM users WHERE username = 'simran'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU007', 'Arjun', 'Verma', 'arjun@campusflow.com', '+91-98765-02007', 'Electronics & Comm', 5, '2024-08-01' FROM users WHERE username = 'arjun'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU008', 'Priya', 'Sharma', 'priya@campusflow.com', '+91-98765-02008', 'Computer Science', 3, '2025-08-01' FROM users WHERE username = 'priya'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU009', 'Rohan', 'Malhotra', 'rohan@campusflow.com', '+91-98765-02009', 'Computer Science', 5, '2024-08-01' FROM users WHERE username = 'rohan'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date)
SELECT id, 'STU010', 'Ananya', 'Gupta', 'ananya@campusflow.com', '+91-98765-02010', 'Information Technology', 3, '2025-08-01' FROM users WHERE username = 'ananya'
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), email = VALUES(email);

-- 4. Demo Academic Courses (Indian Engineering University Curriculum)
INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS101', 'Object-Oriented Programming with Java', 'Core Java OOP concepts, collections, and Swing GUI', 4, 'Computer Science', id, 60 FROM faculty WHERE faculty_id = 'FAC001'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS201', 'Data Structures & Algorithms', 'Linear & non-linear structures, balanced trees, and graph algorithms', 4, 'Computer Science', id, 60 FROM faculty WHERE faculty_id = 'FAC001'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS301', 'Database Management Systems', 'Relational algebra, SQL, normalization, and ACID properties', 4, 'Computer Science', id, 60 FROM faculty WHERE faculty_id = 'FAC001'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS302', 'Operating Systems', 'Process scheduling, virtual memory management, deadlocks, and threads', 4, 'Computer Science', id, 60 FROM faculty WHERE faculty_id = 'FAC002'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'CS303', 'Computer Networks', 'OSI/TCP-IP stacks, routing protocols, flow control, and sockets', 3, 'Computer Science', id, 60 FROM faculty WHERE faculty_id = 'FAC002'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'AI301', 'Artificial Intelligence & ML', 'Search algorithms, regression, classification, and neural networks', 4, 'Computer Science', id, 50 FROM faculty WHERE faculty_id = 'FAC003'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'IT301', 'Web Technologies & Cloud Computing', 'RESTful services, microservices, cloud deployments, and web UI', 3, 'Information Technology', id, 50 FROM faculty WHERE faculty_id = 'FAC004'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'EC101', 'Network Analysis & Synthesis', 'Two-port networks, Laplace transforms, and frequency filters', 3, 'Electronics & Comm', id, 45 FROM faculty WHERE faculty_id = 'FAC005'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students)
SELECT 'EC201', 'Digital Electronics & Logic Design', 'Boolean algebra, combinational & sequential logic, and FPGA', 4, 'Electronics & Comm', id, 45 FROM faculty WHERE faculty_id = 'FAC005'
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), credits = VALUES(credits), max_students = VALUES(max_students);

-- 5. Demo Course Enrollments
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A+' FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='A+';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS201' ON DUPLICATE KEY UPDATE grade='A';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A-' FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS301' ON DUPLICATE KEY UPDATE grade='A-';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B+' FROM students s, courses c WHERE s.student_id = 'STU001' AND c.course_code = 'CS302' ON DUPLICATE KEY UPDATE grade='B+';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU003' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='A';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU003' AND c.course_code = 'CS201' ON DUPLICATE KEY UPDATE grade='A';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A-' FROM students s, courses c WHERE s.student_id = 'STU003' AND c.course_code = 'CS301' ON DUPLICATE KEY UPDATE grade='A-';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU004' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='A';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B+' FROM students s, courses c WHERE s.student_id = 'STU004' AND c.course_code = 'CS201' ON DUPLICATE KEY UPDATE grade='B+';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A-' FROM students s, courses c WHERE s.student_id = 'STU004' AND c.course_code = 'CS301' ON DUPLICATE KEY UPDATE grade='A-';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU004' AND c.course_code = 'AI301' ON DUPLICATE KEY UPDATE grade='A';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B+' FROM students s, courses c WHERE s.student_id = 'STU005' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='B+';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A-' FROM students s, courses c WHERE s.student_id = 'STU005' AND c.course_code = 'CS201' ON DUPLICATE KEY UPDATE grade='A-';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU005' AND c.course_code = 'CS302' ON DUPLICATE KEY UPDATE grade='A';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A'  FROM students s, courses c WHERE s.student_id = 'STU002' AND c.course_code = 'EC101' ON DUPLICATE KEY UPDATE grade='A';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'A+' FROM students s, courses c WHERE s.student_id = 'STU002' AND c.course_code = 'EC201' ON DUPLICATE KEY UPDATE grade='A+';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'B+' FROM students s, courses c WHERE s.student_id = 'STU002' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='B+';

INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'C+' FROM students s, courses c WHERE s.student_id = 'STU009' AND c.course_code = 'CS101' ON DUPLICATE KEY UPDATE grade='C+';
INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade)
SELECT s.id, c.id, '2025-08-15', 'ACTIVE', 'C'  FROM students s, courses c WHERE s.student_id = 'STU009' AND c.course_code = 'CS201' ON DUPLICATE KEY UPDATE grade='C';
