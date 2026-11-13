# CampusFlow — College Management System

A desktop application built using **Core Java**, **Java Swing**, **JDBC**, and **MySQL** for managing daily academic operations. CampusFlow provides dedicated portals for Administrators, Faculty, and Students to manage student records, course allocations, and attendance tracking completely offline without requiring internet or web servers.

---

## 📌 Features

### 1. Administrator Portal
* **Student Management:** Add, update, view, and delete student records with department and semester filters.
* **Faculty Management:** Manage faculty profiles, designations, and department mappings.
* **Course & Enrollment Management:** Create courses, assign faculty instructors, set seat limits, and enroll students.
* **College Analytics:** View total active students, faculty count, and campus-wide average attendance.

### 2. Faculty Portal
* **Assigned Courses:** Displays only the courses assigned to the logged-in teacher.
* **Daily Attendance Marking:** Mark student attendance per lecture as `Present`, `Absent`, `Late`, or `Excused`.
* **Batch Database Updates:** Saves attendance for entire classes in a single batch database transaction.
* **Grade Submission:** Enter and update internal grades for enrolled students.

### 3. Student Portal
* **Course Overview:** View all enrolled courses, credit hours, and assigned instructors.
* **Live Attendance Tracking:** Calculates attendance percentage in real-time.
* **75% Attendance Warning:** Displays a green "Good Standing" badge for $\ge 75\%$ attendance and a red alert badge for $< 75\%$ to warn students about semester examination eligibility.
* **Class Schedule:** View weekly lecture timetables and timings.

---

## 🛠 Tech Stack

* **Language:** Core Java (JDK 17+)
* **User Interface:** Java Swing with FlatLaf (Modern Look and Feel)
* **Architecture:** Model-View-Controller (MVC) Pattern
* **Database:** MySQL 8.0 (InnoDB engine with Foreign Keys)
* **Connectivity:** Pure JDBC API (`PreparedStatement`, Transactions with `commit`/`rollback`, and Batch Execution)
* **Security:** jBCrypt (Salted password hashing)
* **Build Tool:** Apache Maven

---

## 🏛 System Architecture (MVC)

CampusFlow follows standard Model-View-Controller separation:
* **Model (`com.campusflow.model`, `com.campusflow.dao`):** Java entity classes (`Student.java`, `Course.java`, `Attendance.java`) and Data Access Objects executing parameterized SQL queries.
* **View (`com.campusflow.view`):** Swing screens and panels (`LoginView`, `AdminDashboardView`, `FacultyDashboardView`, `StudentDashboardView`).
* **Controller / Service (`com.campusflow.service`):** Business logic layer (`AttendanceService`, `StudentService`, `AuthService`) validating inputs, managing transactions, and enforcing academic rules.

---

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK 17 or higher)
* MySQL Server (8.0 or higher)
* Apache Maven (3.8+)

---

### Step 1: Database Setup
1. Start your local MySQL service.
2. Open terminal and run the schema and seed scripts:
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

---

### Step 2: Database Configuration
Update your MySQL username and password in `src/main/resources/application.properties` (or set them via environment variables):
```properties
db.host=localhost
db.port=3306
db.name=campusflow_db
db.username=root
db.password=your_mysql_password
```

---

### Step 3: Run the Application

#### Option A: Run directly with Maven (Development)
```bash
mvn exec:java
```

#### Option B: Run the standalone executable JAR
```bash
java -jar dist/CampusFlow.jar
```

#### Option C: Build from source
```bash
mvn clean package
```
This generates the standalone executable JAR inside the `dist/` directory.

---

## 🔑 Demo Login Credentials

The database seeder includes ready-to-test accounts:

| Role | Username / Email | Password | Name / Access Scope |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | System Administrator (Full Access) |
| **Faculty** | `daulat` or `daulat@campusflow.com` | `faculty123` | Er. Daulat Ram (CSE Department) |
| **Faculty** | `kiran` or `kiran@campusflow.com` | `faculty123` | Er. Kiranpreet Kaur (IT Department) |
| **Student** | `mukul` or `mukul@campusflow.com` | `student123` | Mukul Anand (CSE, Semester 5) |
| **Student** | `mehak` or `mehak@campusflow.com` | `student123` | Mehakpreet Kaur (CSE, Semester 5) |
| **Student** | `divya` or `divya@campusflow.com` | `student123` | Divya Sharma (CSE, Semester 5) |

---

## 📂 Project Directory Structure

```
CampusFlow/
├── src/
│   ├── main/
│   │   ├── java/com/campusflow/
│   │   │   ├── config/          # Database configuration & credentials
│   │   │   ├── dao/             # Pure JDBC Data Access Objects
│   │   │   ├── model/           # POJO Data Models (Entities)
│   │   │   ├── service/         # Business logic & validations
│   │   │   ├── util/            # Password hashing (BCrypt), DB seeder
│   │   │   └── view/            # Java Swing GUI views & dashboard panels
│   │   └── resources/           # Application properties & icons
├── database/
│   ├── schema.sql               # Database tables, keys & constraints
│   └── seed.sql                 # Sample Indian university dataset
├── pom.xml                      # Maven project & dependency configuration
└── README.md                    # Project documentation
```

---

## 👥 Project Team & Credits

**Department of Computer Science & Engineering**  
**Chandigarh University**

* **Mukul Anand (24BCS10390)** — System Architecture, Database Schema, Pure JDBC DAO Layer, BCrypt Security & Build Packaging *(Team Lead)*
* **Mehakpreet Kaur (24BCS11671)** — Student & Faculty Management Modules, Form Validations & Search Filters
* **Divya Sharma (24BCS11668)** — Course Catalog, Student Enrollment System, Attendance Engine & 75% Rule Calculation

**Project Supervisor:** Er. Daulat Ram (E13701)
