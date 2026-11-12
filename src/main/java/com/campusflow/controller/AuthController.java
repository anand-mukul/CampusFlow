package com.campusflow.controller;

import com.campusflow.model.User;
import com.campusflow.service.AuthService;
import com.campusflow.view.AdminDashboardView;
import com.campusflow.view.FacultyDashboardView;
import com.campusflow.view.LoginView;
import com.campusflow.view.StudentDashboardView;

import javax.swing.*;
import java.util.Optional;

/**
 * Controller orchestrating user authentication, session lifecycle,
 * and role-based navigation between Login and Dashboards.
 */
public class AuthController {
    private final AuthService authService;
    private LoginView loginView;
    private JFrame currentDashboard;

    public AuthController() {
        this.authService = new AuthService();
    }

    public void start() {
        showLogin();
    }

    public void showLogin() {
        if (currentDashboard != null) {
            currentDashboard.dispose();
            currentDashboard = null;
        }

        SwingUtilities.invokeLater(() -> {
            if (loginView != null) {
                loginView.dispose();
            }
            loginView = new LoginView();
            loginView.setLoginListener(this::handleLogin);
            loginView.setVisible(true);
        });
    }

    private void handleLogin(String username, String password) {
        if (loginView != null) {
            loginView.setLoading(true);
        }

        SwingWorker<Optional<User>, Void> worker = new SwingWorker<>() {
            private String failureMessage = null;

            @Override
            protected Optional<User> doInBackground() {
                try {
                    return authService.login(username, password);
                } catch (Exception ex) {
                    System.err.println("Login error: " + ex.getMessage());
                    failureMessage = "Database error: Unable to connect to MySQL server.";
                    return Optional.empty();
                }
            }

            @Override
            protected void done() {
                if (loginView == null) return;
                loginView.setLoading(false);

                try {
                    Optional<User> userOpt = get();
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        loginView.setVisible(false);
                        loginView.dispose();
                        loginView = null;
                        navigateToDashboard(user);
                    } else {
                        if (failureMessage != null) {
                            loginView.showError(failureMessage);
                        } else {
                            loginView.showError("Invalid username or password. Please try again.");
                        }
                    }
                } catch (Exception e) {
                    loginView.showError("Authentication failed: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void navigateToDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            if (currentDashboard != null) {
                currentDashboard.dispose();
            }

            Runnable logoutCallback = this::handleLogout;

            switch (user.getRole()) {
                case ADMIN:
                    currentDashboard = new AdminDashboardView(user, logoutCallback);
                    break;
                case FACULTY:
                    currentDashboard = new FacultyDashboardView(user, logoutCallback);
                    break;
                case STUDENT:
                    currentDashboard = new StudentDashboardView(user, logoutCallback);
                    break;
                default:
                    JOptionPane.showMessageDialog(
                            null,
                            "Unrecognized user role: " + user.getRole(),
                            "Access Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    showLogin();
                    return;
            }

            currentDashboard.setVisible(true);
        });
    }

    public void handleLogout() {
        authService.logout();
        showLogin();
    }

    public AuthService getAuthService() {
        return authService;
    }
}
