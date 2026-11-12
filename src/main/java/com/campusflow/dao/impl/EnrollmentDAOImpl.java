package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.EnrollmentDAO;
import com.campusflow.model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE student_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrollments by student: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int countByStudentId(int studentId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting student enrollments: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Enrollment> findByCourseId(int courseId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE course_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrollments by course: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int countByCourseId(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting course enrollments: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all enrollments: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean isAlreadyEnrolled(int studentId, int courseId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking existing enrollment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean save(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status, grade) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getCourseId());
            ps.setDate(3, enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate() : new Date(System.currentTimeMillis()));
            ps.setString(4, enrollment.getStatus() != null ? enrollment.getStatus() : "ACTIVE");
            ps.setString(5, enrollment.getGrade());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        enrollment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving enrollment: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateStatus(int studentId, int courseId, String status, String grade) {
        String sql = "UPDATE enrollments SET status = ?, grade = ? WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, grade);
            ps.setInt(3, studentId);
            ps.setInt(4, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating enrollment status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int studentId, int courseId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting enrollment: " + e.getMessage());
            return false;
        }
    }

    private Enrollment mapResultSet(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setEnrollmentDate(rs.getDate("enrollment_date"));
        e.setStatus(rs.getString("status"));
        e.setGrade(rs.getString("grade"));
        return e;
    }
}
