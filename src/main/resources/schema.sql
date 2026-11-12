-- CampusFlow Database Schema
-- Run this script to initialize the database

CREATE DATABASE IF NOT EXISTS campusflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campusflow_db;

CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    role         ENUM('ADMIN','FACULTY','STUDENT') NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    student_id      VARCHAR(20)  NOT NULL UNIQUE,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(15),
    department      VARCHAR(100),
    semester        INT          NOT NULL DEFAULT 1,
    enrollment_date DATE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS faculty (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    faculty_id  VARCHAR(20)  NOT NULL UNIQUE,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(15),
    department  VARCHAR(100),
    designation VARCHAR(100),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS courses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20)  NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    credits     INT          NOT NULL DEFAULT 3,
    department  VARCHAR(100),
    faculty_id  INT,
    max_students INT         NOT NULL DEFAULT 60,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    course_id       INT NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    status          ENUM('ACTIVE','DROPPED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    grade           VARCHAR(5),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_enrollment (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT  NOT NULL,
    course_id       INT  NOT NULL,
    attendance_date DATE NOT NULL,
    status          ENUM('PRESENT','ABSENT','LATE','LEAVE') NOT NULL,
    marked_by       INT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attendance (student_id, course_id, attendance_date),
    FOREIGN KEY (student_id) REFERENCES students(id)  ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)   ON DELETE CASCADE,
    FOREIGN KEY (marked_by)  REFERENCES users(id)     ON DELETE SET NULL
);

