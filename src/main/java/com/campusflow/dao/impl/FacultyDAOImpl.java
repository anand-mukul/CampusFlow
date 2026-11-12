package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.FacultyDAO;
import com.campusflow.model.Faculty;

import java.sql.*;
import java.util.*;

public class FacultyDAOImpl implements FacultyDAO {

    @Override
    public List<Faculty> findAll() {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM faculty WHERE is_active = TRUE ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching faculty: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Faculty> findById(int id) {
        String sql = "SELECT * FROM faculty WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding faculty: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Faculty> findByUserId(int userId) {
        String sql = "SELECT * FROM faculty WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding faculty by userId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Faculty> findByFacultyId(String facultyId) {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding faculty by facultyId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM faculty WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting faculty: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean save(Faculty faculty) {
        String sql = "INSERT INTO faculty (user_id, faculty_id, first_name, last_name, email, phone, department, designation, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, faculty.getUserId());
            ps.setString(2, faculty.getFacultyId());
            ps.setString(3, faculty.getFirstName());
            ps.setString(4, faculty.getLastName());
            ps.setString(5, faculty.getEmail());
            ps.setString(6, faculty.getPhone());
            ps.setString(7, faculty.getDepartment());
            ps.setString(8, faculty.getDesignation());
            ps.setBoolean(9, faculty.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) faculty.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving faculty: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Faculty faculty) {
        String sql = "UPDATE faculty SET faculty_id = ?, first_name = ?, last_name = ?, email = ?, phone = ?, " +
                     "department = ?, designation = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faculty.getFacultyId());
            ps.setString(2, faculty.getFirstName());
            ps.setString(3, faculty.getLastName());
            ps.setString(4, faculty.getEmail());
            ps.setString(5, faculty.getPhone());
            ps.setString(6, faculty.getDepartment());
            ps.setString(7, faculty.getDesignation());
            ps.setBoolean(8, faculty.isActive());
            ps.setInt(9, faculty.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating faculty: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM faculty WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting faculty: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Faculty> search(String keyword, String department) {
        List<Faculty> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM faculty WHERE is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(faculty_id) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(designation) LIKE ?) ");
            String pat = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pat); params.add(pat); params.add(pat); params.add(pat); params.add(pat);
        }

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND department = ? ");
            params.add(department.trim());
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
            System.err.println("Error searching faculty: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean isFacultyIdTaken(String facultyId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM faculty WHERE faculty_id = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facultyId.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking faculty ID: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isEmailTaken(String email, int excludeId) {
        String sql = "SELECT COUNT(*) FROM faculty WHERE email = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking faculty email: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String generateNextFacultyId() {
        String sql = "SELECT faculty_id FROM faculty ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1); // e.g. FAC002
                if (lastId != null && lastId.matches("FAC\\d+")) {
                    int num = Integer.parseInt(lastId.substring(3));
                    return String.format("FAC%03d", num + 1);
                }
            }
        } catch (Exception ignored) {}
        return "FAC001";
    }

    @Override
    public boolean hasAssignedCourses(int facultyId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE faculty_id = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    @Override
    public List<String> getDistinctDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM faculty WHERE department IS NOT NULL AND department != '' ORDER BY department";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException ignored) {}
        return list;
    }

    private Faculty mapRow(ResultSet rs) throws SQLException {
        Faculty f = new Faculty();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setFacultyId(rs.getString("faculty_id"));
        f.setFirstName(rs.getString("first_name"));
        f.setLastName(rs.getString("last_name"));
        f.setEmail(rs.getString("email"));
        f.setPhone(rs.getString("phone"));
        f.setDepartment(rs.getString("department"));
        f.setDesignation(rs.getString("designation"));
        f.setActive(rs.getBoolean("is_active"));
        return f;
    }
}
