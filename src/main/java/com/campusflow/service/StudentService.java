package com.campusflow.service;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.StudentDAO;
import com.campusflow.dao.UserDAO;
import com.campusflow.dao.impl.StudentDAOImpl;
import com.campusflow.dao.impl.UserDAOImpl;
import com.campusflow.model.Student;
import com.campusflow.model.User;
import com.campusflow.util.PasswordUtil;
import com.campusflow.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;

    public StudentService() {
        this.studentDAO = new StudentDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    public StudentService(StudentDAO studentDAO, UserDAO userDAO) {
        this.studentDAO = studentDAO;
        this.userDAO = userDAO;
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public int getStudentCount() {
        return studentDAO.count();
    }

    public Optional<Student> getStudentById(int id) {
        return studentDAO.findById(id);
    }

    public Optional<Student> getStudentByUserId(int userId) {
        return studentDAO.findByUserId(userId);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentDAO.findByStudentId(studentId);
    }

    public String generateNextStudentId() {
        return studentDAO.generateNextStudentId();
    }

    public List<String> getDepartments() {
        return studentDAO.getDistinctDepartments();
    }

    public List<Student> searchStudents(String keyword, String department, Integer semester) {
        return studentDAO.search(keyword, department, semester);
    }

    /**
     * Registers a new student and creates their User credentials in a database transaction.
     */
    public ServiceResult registerStudent(Student student, String plainPassword) {
        // Validation
        if (student == null) return ServiceResult.fail("Student information cannot be null.");
        if (ValidationUtil.isNullOrEmpty(student.getFirstName())) return ServiceResult.fail("First name is required.");
        if (ValidationUtil.isNullOrEmpty(student.getLastName())) return ServiceResult.fail("Last name is required.");
        if (ValidationUtil.isNullOrEmpty(student.getStudentId())) return ServiceResult.fail("Student ID is required.");
        if (!ValidationUtil.isValidEmail(student.getEmail())) return ServiceResult.fail("A valid email address is required.");

        if (studentDAO.isStudentIdTaken(student.getStudentId(), 0)) {
            return ServiceResult.fail("Student ID '" + student.getStudentId() + "' is already registered.");
        }
        if (studentDAO.isEmailTaken(student.getEmail(), 0)) {
            return ServiceResult.fail("Email address '" + student.getEmail() + "' is already in use.");
        }

        String password = (plainPassword != null && !plainPassword.trim().isEmpty()) ? plainPassword.trim() : "student123";

        // Transactional creation of User + Student
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create User
                String username = student.getStudentId().toLowerCase();
                User user = new User();
                user.setUsername(username);
                user.setEmail(student.getEmail());
                user.setPasswordHash(PasswordUtil.hashPassword(password));
                user.setRole(User.Role.STUDENT);
                user.setActive(true);

                if (!userDAO.save(user)) {
                    conn.rollback();
                    return ServiceResult.fail("Failed to create user authentication record.");
                }

                // 2. Create Student with generated user_id
                student.setUserId(user.getId());
                if (!studentDAO.save(student)) {
                    conn.rollback();
                    return ServiceResult.fail("Failed to save student profile.");
                }

                conn.commit();
                return ServiceResult.ok("Student registered successfully! (Username: " + username + ")");
            } catch (Exception ex) {
                conn.rollback();
                return ServiceResult.fail("Database transaction error: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return ServiceResult.fail("Database connection failed: " + e.getMessage());
        }
    }

    public ServiceResult updateStudent(Student student) {
        if (student == null || student.getId() <= 0) return ServiceResult.fail("Invalid student record.");
        if (ValidationUtil.isNullOrEmpty(student.getFirstName())) return ServiceResult.fail("First name is required.");
        if (ValidationUtil.isNullOrEmpty(student.getLastName())) return ServiceResult.fail("Last name is required.");
        if (ValidationUtil.isNullOrEmpty(student.getStudentId())) return ServiceResult.fail("Student ID is required.");
        if (!ValidationUtil.isValidEmail(student.getEmail())) return ServiceResult.fail("A valid email address is required.");

        if (studentDAO.isStudentIdTaken(student.getStudentId(), student.getId())) {
            return ServiceResult.fail("Student ID '" + student.getStudentId() + "' is already taken by another student.");
        }
        if (studentDAO.isEmailTaken(student.getEmail(), student.getId())) {
            return ServiceResult.fail("Email address '" + student.getEmail() + "' is already taken by another student.");
        }

        boolean updated = studentDAO.update(student);
        if (updated) {
            // Also update linked user's email if userId is present
            if (student.getUserId() > 0) {
                userDAO.findById(student.getUserId()).ifPresent(u -> {
                    u.setEmail(student.getEmail());
                    userDAO.update(u);
                });
            }
            return ServiceResult.ok("Student record updated successfully.");
        } else {
            return ServiceResult.fail("Failed to update student record.");
        }
    }

    public ServiceResult deleteStudent(int studentId) {
        if (studentId <= 0) return ServiceResult.fail("Invalid student ID.");

        if (studentDAO.hasEnrollments(studentId)) {
            return ServiceResult.fail("Cannot delete student: Active course enrollments exist. Please drop enrollments first.");
        }
        if (studentDAO.hasAttendance(studentId)) {
            return ServiceResult.fail("Cannot delete student: Academic attendance records exist for this student.");
        }

        Optional<Student> sOpt = studentDAO.findById(studentId);
        boolean deleted = studentDAO.delete(studentId);
        if (deleted) {
            // If user record exists, remove it too
            sOpt.ifPresent(s -> {
                if (s.getUserId() > 0) {
                    try (Connection conn = DatabaseConfig.getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        ps.setInt(1, s.getUserId());
                        ps.executeUpdate();
                    } catch (Exception ignored) {}
                }
            });
            return ServiceResult.ok("Student deleted successfully.");
        } else {
            return ServiceResult.fail("Failed to delete student.");
        }
    }
}
