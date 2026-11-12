package com.campusflow.service;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.dao.UserDAO;
import com.campusflow.dao.impl.FacultyDAOImpl;
import com.campusflow.dao.impl.UserDAOImpl;
import com.campusflow.model.Faculty;
import com.campusflow.model.User;
import com.campusflow.util.PasswordUtil;
import com.campusflow.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FacultyService {
    private final FacultyDAO facultyDAO;
    private final UserDAO userDAO;

    public FacultyService() {
        this.facultyDAO = new FacultyDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    public FacultyService(FacultyDAO facultyDAO, UserDAO userDAO) {
        this.facultyDAO = facultyDAO;
        this.userDAO = userDAO;
    }

    public List<Faculty> getAllFaculty() {
        return facultyDAO.findAll();
    }

    public int getFacultyCount() {
        return facultyDAO.count();
    }

    public Optional<Faculty> getFacultyById(int id) {
        return facultyDAO.findById(id);
    }

    public Optional<Faculty> getFacultyByUserId(int userId) {
        return facultyDAO.findByUserId(userId);
    }

    public Optional<Faculty> getFacultyByFacultyId(String facultyId) {
        return facultyDAO.findByFacultyId(facultyId);
    }

    public String generateNextFacultyId() {
        return facultyDAO.generateNextFacultyId();
    }

    public List<String> getDepartments() {
        return facultyDAO.getDistinctDepartments();
    }

    public List<Faculty> searchFaculty(String keyword, String department) {
        return facultyDAO.search(keyword, department);
    }

    /**
     * Registers a new faculty member and creates their User credentials in a database transaction.
     */
    public ServiceResult registerFaculty(Faculty faculty, String plainPassword) {
        if (faculty == null) return ServiceResult.fail("Faculty information cannot be null.");
        if (ValidationUtil.isNullOrEmpty(faculty.getFirstName())) return ServiceResult.fail("First name is required.");
        if (ValidationUtil.isNullOrEmpty(faculty.getLastName())) return ServiceResult.fail("Last name is required.");
        if (ValidationUtil.isNullOrEmpty(faculty.getFacultyId())) return ServiceResult.fail("Faculty ID is required.");
        if (!ValidationUtil.isValidEmail(faculty.getEmail())) return ServiceResult.fail("A valid email address is required.");

        if (facultyDAO.isFacultyIdTaken(faculty.getFacultyId(), 0)) {
            return ServiceResult.fail("Faculty ID '" + faculty.getFacultyId() + "' is already registered.");
        }
        if (facultyDAO.isEmailTaken(faculty.getEmail(), 0)) {
            return ServiceResult.fail("Email address '" + faculty.getEmail() + "' is already in use.");
        }

        String password = (plainPassword != null && !plainPassword.trim().isEmpty()) ? plainPassword.trim() : "faculty123";

        // Transactional creation of User + Faculty
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create User
                String username = faculty.getFacultyId().toLowerCase();
                User user = new User();
                user.setUsername(username);
                user.setEmail(faculty.getEmail());
                user.setPasswordHash(PasswordUtil.hashPassword(password));
                user.setRole(User.Role.FACULTY);
                user.setActive(true);

                if (!userDAO.save(user)) {
                    conn.rollback();
                    return ServiceResult.fail("Failed to create user authentication record.");
                }

                // 2. Create Faculty with generated user_id
                faculty.setUserId(user.getId());
                if (!facultyDAO.save(faculty)) {
                    conn.rollback();
                    return ServiceResult.fail("Failed to save faculty profile.");
                }

                conn.commit();
                return ServiceResult.ok("Faculty registered successfully! (Username: " + username + ")");
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

    public ServiceResult updateFaculty(Faculty faculty) {
        if (faculty == null || faculty.getId() <= 0) return ServiceResult.fail("Invalid faculty record.");
        if (ValidationUtil.isNullOrEmpty(faculty.getFirstName())) return ServiceResult.fail("First name is required.");
        if (ValidationUtil.isNullOrEmpty(faculty.getLastName())) return ServiceResult.fail("Last name is required.");
        if (ValidationUtil.isNullOrEmpty(faculty.getFacultyId())) return ServiceResult.fail("Faculty ID is required.");
        if (!ValidationUtil.isValidEmail(faculty.getEmail())) return ServiceResult.fail("A valid email address is required.");

        if (facultyDAO.isFacultyIdTaken(faculty.getFacultyId(), faculty.getId())) {
            return ServiceResult.fail("Faculty ID '" + faculty.getFacultyId() + "' is already assigned to another instructor.");
        }
        if (facultyDAO.isEmailTaken(faculty.getEmail(), faculty.getId())) {
            return ServiceResult.fail("Email address '" + faculty.getEmail() + "' is already registered to another user.");
        }

        boolean updated = facultyDAO.update(faculty);
        if (updated) {
            if (faculty.getUserId() > 0) {
                userDAO.findById(faculty.getUserId()).ifPresent(u -> {
                    u.setEmail(faculty.getEmail());
                    userDAO.update(u);
                });
            }
            return ServiceResult.ok("Faculty profile updated successfully.");
        } else {
            return ServiceResult.fail("Failed to update faculty profile.");
        }
    }

    public ServiceResult deleteFaculty(int facultyId) {
        if (facultyId <= 0) return ServiceResult.fail("Invalid faculty ID.");

        if (facultyDAO.hasAssignedCourses(facultyId)) {
            return ServiceResult.fail("Cannot delete faculty member: Active courses are assigned to this instructor. Please reassign the courses first.");
        }

        Optional<Faculty> fOpt = facultyDAO.findById(facultyId);
        boolean deleted = facultyDAO.delete(facultyId);
        if (deleted) {
            fOpt.ifPresent(f -> {
                if (f.getUserId() > 0) {
                    try (Connection conn = DatabaseConfig.getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        ps.setInt(1, f.getUserId());
                        ps.executeUpdate();
                    } catch (Exception ignored) {}
                }
            });
            return ServiceResult.ok("Faculty member deleted successfully.");
        } else {
            return ServiceResult.fail("Failed to delete faculty member.");
        }
    }
}
