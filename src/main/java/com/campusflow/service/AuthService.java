package com.campusflow.service;

import com.campusflow.dao.UserDAO;
import com.campusflow.dao.impl.UserDAOImpl;
import com.campusflow.model.User;
import com.campusflow.util.PasswordUtil;

import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public Optional<User> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                this.currentUser = user;
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}

