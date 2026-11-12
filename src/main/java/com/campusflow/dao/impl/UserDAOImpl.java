package com.campusflow.dao.impl;

import com.campusflow.config.DatabaseConfig;
import com.campusflow.dao.UserDAO;
import com.campusflow.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, email, role, is_active, created_at, updated_at FROM users WHERE (username = ? OR email = ?) AND is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT id, username, password_hash, email, role, is_active, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password_hash, email, role, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET email = ?, is_active = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setBoolean(2, user.isActive());
            ps.setInt(3, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}

