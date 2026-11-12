package com.campusflow.model;

import java.sql.Timestamp;

public class User {
    public enum Role { ADMIN, FACULTY, STUDENT }

    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private Role role;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public User(int id, String username, String passwordHash, String email, Role role, boolean active) {
        this.id = id; this.username = username; this.passwordHash = passwordHash;
        this.email = email; this.role = role; this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override public String toString() { return "User{id=" + id + ", username='" + username + "', role=" + role + "}"; }
}

