package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.CourseDAO;
import com.campusflow.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDAOImpl implements CourseDAO {

    @Override
    public List<Course> findAll() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching courses: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Course> findById(int id) {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding course by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        String sql = "SELECT * FROM courses WHERE course_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding course by code: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Course> findByFacultyId(int facultyId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE faculty_id = ? AND is_active = TRUE ORDER BY course_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding courses by faculty: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Course> search(String keyword, String department) {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM courses WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append(" AND (course_code LIKE ? OR course_name LIKE ? OR description LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (department != null && !department.trim().isEmpty()) {
            sql.append(" AND department = ?");
            params.add(department.trim());
        }

        sql.append(" ORDER BY course_name");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching courses: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean isCourseCodeTaken(String courseCode, int excludeId) {
        String sql = "SELECT 1 FROM courses WHERE course_code = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking course code: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean save(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, description, credits, department, faculty_id, max_students, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setString(3, course.getDescription());
            ps.setInt(4, course.getCredits());
            ps.setString(5, course.getDepartment());
            if (course.getFacultyId() > 0) {
                ps.setInt(6, course.getFacultyId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, course.getMaxStudents() > 0 ? course.getMaxStudents() : 60);
            ps.setBoolean(8, course.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        course.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving course: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, description = ?, credits = ?, " +
                "department = ?, faculty_id = ?, max_students = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setString(3, course.getDescription());
            ps.setInt(4, course.getCredits());
            ps.setString(5, course.getDepartment());
            if (course.getFacultyId() > 0) {
                ps.setInt(6, course.getFacultyId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, course.getMaxStudents());
            ps.setBoolean(8, course.isActive());
            ps.setInt(9, course.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasEnrollments(int courseId) {
        String sql = "SELECT 1 FROM enrollments WHERE course_id = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking enrollments: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int getEnrolledCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting enrolled students: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<String> getDistinctDepartments() {
        List<String> depts = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM courses WHERE department IS NOT NULL AND department != '' ORDER BY department";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                depts.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching distinct course departments: " + e.getMessage());
        }
        return depts;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM courses WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting courses: " + e.getMessage());
        }
        return 0;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setDescription(rs.getString("description"));
        c.setCredits(rs.getInt("credits"));
        c.setDepartment(rs.getString("department"));
        c.setFacultyId(rs.getInt("faculty_id"));
        c.setMaxStudents(rs.getInt("max_students"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
