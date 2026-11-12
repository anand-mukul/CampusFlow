package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.StudentDAO;
import com.campusflow.model.Student;

import java.sql.*;
import java.util.*;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE is_active = TRUE ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching students: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Student> findById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByUserId(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by userId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByStudentId(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by studentId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM students WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting students: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean save(Student student) {
        String sql = "INSERT INTO students (user_id, student_id, first_name, last_name, email, phone, department, semester, enrollment_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, student.getUserId());
            ps.setString(2, student.getStudentId());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getLastName());
            ps.setString(5, student.getEmail());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getDepartment());
            ps.setInt(8, student.getSemester());
            ps.setDate(9, student.getEnrollmentDate() != null ? student.getEnrollmentDate() : new java.sql.Date(System.currentTimeMillis()));
            ps.setBoolean(10, student.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) student.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving student: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET student_id = ?, first_name = ?, last_name = ?, email = ?, phone = ?, " +
                     "department = ?, semester = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getDepartment());
            ps.setInt(7, student.getSemester());
            ps.setBoolean(8, student.isActive());
            ps.setInt(9, student.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        // Safe delete: set inactive or delete if no FK constraints
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Student> search(String keyword, String department, Integer semester) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(student_id) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ?) ");
            String pat = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pat); params.add(pat); params.add(pat); params.add(pat);
        }

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND department = ? ");
            params.add(department.trim());
        }

        if (semester != null && semester > 0) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }

        sql.append("ORDER BY id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching students: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean isStudentIdTaken(String studentId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking student ID: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isEmailTaken(String email, int excludeId) {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking student email: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String generateNextStudentId() {
        String sql = "SELECT student_id FROM students ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1); // e.g. STU003
                if (lastId != null && lastId.matches("STU\\d+")) {
                    int num = Integer.parseInt(lastId.substring(3));
                    return String.format("STU%03d", num + 1);
                }
            }
        } catch (Exception ignored) {}
        return "STU001";
    }

    @Override
    public boolean hasEnrollments(int studentId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    @Override
    public boolean hasAttendance(int studentId) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    @Override
    public List<String> getDistinctDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM students WHERE department IS NOT NULL AND department != '' ORDER BY department";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException ignored) {}
        return list;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStudentId(rs.getString("student_id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setSemester(rs.getInt("semester"));
        s.setEnrollmentDate(rs.getDate("enrollment_date"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}
