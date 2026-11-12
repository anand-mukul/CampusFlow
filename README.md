# CampusFlow — Smart College Management System

> A professional desktop-based college management platform built with Core Java, Java Swing, JDBC, and MySQL.

---

## Overview

CampusFlow is a comprehensive Smart College Management System that provides **role-based dashboards** for Admins, Faculty, and Students. It strictly follows the **MVC (Model-View-Controller)** architectural pattern with clean layer separation (`View` → `Controller` → `Service` → `DAO` → `MySQL Database`). The UI is built using Java Swing with FlatLaf modern look-and-feel, rounded components, and responsive cards.

---

## Technology Stack

| Component            | Technology                        |
|----------------------|-----------------------------------|
| Language             | Java 17+ (JDK 25 runtime support) |
| UI Framework         | Java Swing (Pure Desktop)         |
| Database             | MySQL 8.x                         |
| DB Connectivity      | JDBC (PreparedStatement & Txns)   |
| Build Tool           | Apache Maven                      |
| Password Hashing     | BCrypt via jBCrypt 0.4            |
| Look & Feel          | FlatLaf 3.2.5 Modern SaaS Theme   |

---

## Architecture & Project Structure

CampusFlow strictly keeps SQL out of Swing views:
```
src/main/java/com/campusflow/
├── config/           — Application & database configuration
│   ├── AppConfig.java          — Design tokens, colors, fonts, global constants
│   └── DatabaseConfig.java     — Centralized JDBC connection pool management
│
├── model/            — Entity classes (POJOs) & Report DTOs
│   ├── User.java               — Authentication user (Admin, Faculty, Student)
│   ├── Student.java            — Student profile (student_id, name, department, semester)
│   ├── Faculty.java            — Faculty profile (faculty_id, name, designation, department)
│   ├── Course.java             — Course entity (course_code, name, credits, faculty_id, max_students)
│   ├── Enrollment.java         — Student enrollment record (status, grade)
│   ├── Attendance.java         — Attendance record (status: PRESENT, ABSENT, LATE, LEAVE)
│   └── report/
│       ├── DefaulterReportItem.java  — Defaulter student item (< 75% attendance)
│       ├── CourseEnrollmentStat.java — Course enrollment occupancy & capacity
│       └── CourseAttendanceStat.java — Course-wise session counts and avg attendance
│
├── dao/              — Data Access Object interfaces
│   ├── UserDAO.java, StudentDAO.java, FacultyDAO.java
│   ├── CourseDAO.java, EnrollmentDAO.java, AttendanceDAO.java, ReportDAO.java
│   └── impl/         — JDBC implementations using PreparedStatement
│
├── service/          — Transactional business logic & validation
│   ├── AuthService.java        — Session lifecycle and BCrypt verification
│   ├── StudentService.java      — Transactional student registration and duplicate prevention
│   ├── FacultyService.java      — Transactional faculty registration and course check
│   ├── CourseService.java       — Course creation, faculty assignment, capacity check
│   ├── EnrollmentService.java   — Duplicate enrollment prevention & capacity validation
│   ├── AttendanceService.java   — Role-based attendance marking and percentage calculation
│   ├── ReportService.java       — Analytics, defaulter query, system metrics
│   └── ServiceResult.java       — Standardized service response wrapper
│
├── controller/       — Application controllers
│   └── AuthController.java     — Authentication lifecycle & dashboard switcher
│
├── view/             — Java Swing UI views & components
│   ├── LoginView.java
│   ├── AdminDashboardView.java
│   ├── FacultyDashboardView.java
│   ├── StudentDashboardView.java
│   ├── components/   — ModernButton, RoundedPanel, Sidebar, StatCard, TopBar
│   ├── student/      — StudentManagementPanel, StudentFormDialog, StudentProfileDialog
│   ├── faculty/      — FacultyManagementPanel, FacultyFormDialog, FacultyProfileDialog
│   ├── course/       — CourseManagementPanel, CourseFormDialog
│   ├── enrollment/   — EnrollmentManagementPanel, EnrollStudentDialog
│   ├── attendance/   — AttendanceManagementPanel, StudentAttendanceHistoryPanel
│   └── reports/      — ReportsPanel (Defaulter Watchlist, Enrollment Stats, Attendance Analytics)
│
└── util/             — Utilities
    ├── PasswordUtil.java       — BCrypt hashing & verification
    ├── ValidationUtil.java     — Form validations (email, phone, student ID)
    └── DatabaseSeeder.java     — Schema verification, migration & demo seed data
```

---

## Phase 2 Modules & Feature Implementation

### Module A — Student & Faculty Management
- **Full CRUD for Students:**
  - Auto-generated or custom Student ID (`STU-001`, etc.).
  - Duplicate email and student ID prevention.
  - Search by name, ID, or email; filter by department and semester.
  - Student Profile Modal displaying personal info, enrolled courses, and real attendance %.
  - Relationship-safe deletion check.
- **Full CRUD for Faculty:**
  - Auto-generated or custom Faculty ID (`FAC-001`, etc.).
  - Transactional user creation for faculty portal login.
  - Search and filter by department.
  - Faculty Profile Modal displaying teaching roster and enrolled student counts.
  - Safe deletion preventing deletion of faculty assigned to active courses.

### Module B — Course, Enrollment & Attendance
- **Course Management:**
  - Create and edit courses with Course Code, Course Name, Credits, Department, Faculty assignment, Max Students.
  - Duplicate Course Code prevention.
  - Relationship-safe deletion (cannot delete courses with enrolled students).
- **Enrollment Management:**
  - Enroll students into courses with duplicate enrollment prevention.
  - Course capacity validation against `courses.max_students`.
  - Drop students and update enrollment status (`ACTIVE`, `COMPLETED`, `DROPPED`) and grades.
- **Attendance Management:**
  - Role-based authorization: Faculty can record attendance only for their assigned courses; Admin can manage all.
  - Interactive table supporting `PRESENT`, `ABSENT`, and `LEAVE`.
  - Quick actions: "Mark All Present", "Mark All Absent", "Save Attendance".
  - Duplicate prevention per student, course, and date using `INSERT ... ON DUPLICATE KEY UPDATE`.
  - **Attendance Calculation Formula:**
    $$\text{Attendance Percentage} = \frac{\text{Present Classes}}{\text{Eligible Conducted Classes}} \times 100$$
    where:
    $$\text{Eligible Conducted Classes} = \text{Total Classes Conducted} - \text{Excused Leaves}$$
    If $\text{Eligible Conducted Classes} \le 0$, attendance rate defaults to $100.0\%$.

### Module C — Authentication, Database & Reports
- **BCrypt Authentication & Authorization:**
  - Admin has global administration rights.
  - Faculty accesses only assigned courses and class rosters.
  - Students access only their personal enrollments, schedule, and attendance records.
- **Real-Time Analytics & Reports (`ReportsPanel`):**
  - **Tab 1: Defaulter Watchlist:** Real-time query identifying students with attendance strictly $< 75\%$ across courses.
  - **Tab 2: Course Capacity & Enrollment:** Course occupancy rates, total seats vs enrolled seats.
  - **Tab 3: Course Attendance Summary:** Conducted sessions and aggregate attendance percentages.

---

## Database Configuration

Default database settings in [DatabaseConfig.java](file:///f:/devlopment/CampusFlow/src/main/java/com/campusflow/config/DatabaseConfig.java):

| Parameter | Default Value | Override via System Property |
|---|---|---|
| Host | `localhost` | `-Ddb.host=...` |
| Port | `3306` | `-Ddb.port=...` |
| Database | `campusflow_db` | `-Ddb.name=...` |
| User | `root` | `-Ddb.user=...` |
| Password | `rootpass` | `-Ddb.password=...` |

---

## Build & Run Instructions

### 1. Prerequisites
- JDK 17 or higher (tested with JDK 25 on Windows)
- Apache Maven 3.6+
- MySQL 8.x service running on port 3306

### 2. Compile & Package
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
cd f:\devlopment\CampusFlow
& "E:\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd" clean compile -o
& "C:\Program Files\Java\jdk-25.0.4.1\bin\jar.exe" --create --file "target/campusflow-1.0.0.jar" --main-class com.campusflow.Main -C "target/classes" .
```

### 3. Run Live Integration Tests
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& "C:\Program Files\Java\jdk-25.0.4.1\bin\java.exe" -cp "target/classes;target/test-classes;C:\Users\mukul\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar;C:\Users\mukul\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;C:\Users\mukul\.m2\repository\com\formdev\flatlaf\3.2.5\flatlaf-3.2.5.jar" com.campusflow.Phase2LiveIntegrationTest
```
*(All 15 integration tests pass cleanly).*

### 4. Run Desktop Application
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& "C:\Program Files\Java\jdk-25.0.4.1\bin\java.exe" -cp "target/campusflow-1.0.0.jar;C:\Users\mukul\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar;C:\Users\mukul\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;C:\Users\mukul\.m2\repository\com\formdev\flatlaf\3.2.5\flatlaf-3.2.5.jar" com.campusflow.Main
```

### 5. Demo Accounts

| Role | Username | Password |
|---|---|---|
| **Admin** | `admin` | `admin123` |
| **Faculty** | `daulat` or `kiranpreet` | `faculty123` |
| **Student** | `mukul` or `mehak` or `divya` | `student123` |
