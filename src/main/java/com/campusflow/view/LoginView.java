package com.campusflow.view;

import com.campusflow.config.AppConfig;
import com.campusflow.view.components.ModernButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern SaaS Split-Panel Login Screen for CampusFlow.
 * Includes username, password, show/hide password toggle, validation messages,
 * and convenient demo account quick-fill chips.
 */
public class LoginView extends JFrame {

    public interface LoginListener {
        void onLogin(String username, String password);
    }

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton togglePasswordBtn;
    private JLabel errorLabel;
    private ModernButton signInBtn;
    private boolean passwordVisible = false;
    private LoginListener loginListener;

    public LoginView() {
        setTitle(AppConfig.APP_NAME + " - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new Dimension(840, 560));
        setLocationRelativeTo(null);
        setBackground(AppConfig.BACKGROUND_COLOR);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new GridLayout(1, 2));
        rootPanel.setBackground(AppConfig.BACKGROUND_COLOR);

        // --- LEFT BRAND HERO PANEL ---
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Elegant diagonal gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, AppConfig.PRIMARY_COLOR,
                        getWidth(), getHeight(), AppConfig.SECONDARY_COLOR);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative ambient circles
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-80, -80, 260, 260);
                g2.setColor(new Color(0x00, 0xB4, 0xD8, 30));
                g2.fillOval(getWidth() - 160, getHeight() - 180, 240, 240);

                g2.dispose();
            }
        };
        heroPanel.setLayout(new BorderLayout());
        heroPanel.setBorder(new EmptyBorder(48, 48, 40, 48));

        // Brand center content
        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));

        // App logo icon badge
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(54, 54));
        logoBadge.setMaximumSize(new Dimension(54, 54));
        logoBadge.setLayout(new GridBagLayout());
        JLabel logoSymbol = new JLabel("CF");
        logoSymbol.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoSymbol.setForeground(Color.WHITE);
        logoBadge.add(logoSymbol);
        logoBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroContent.add(logoBadge);
        heroContent.add(Box.createVerticalStrut(24));

        JLabel heroTitle = new JLabel("CampusFlow");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroContent.add(heroTitle);

        JLabel heroTagline = new JLabel("Smart College Management System");
        heroTagline.setFont(new Font("SansSerif", Font.PLAIN, 15));
        heroTagline.setForeground(new Color(0xBAE6FD));
        heroTagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroContent.add(heroTagline);
        heroContent.add(Box.createVerticalStrut(36));

        // Feature list
        String[] features = {
                "Integrated Student & Faculty Portals",
                "Course Enrollment & Schedule Management",
                "Automated Attendance Tracking",
                "Role-Based Secure Architecture"
        };
        for (String feat : features) {
            JPanel fRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            fRow.setOpaque(false);
            fRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel check = new JLabel("•");
            check.setFont(new Font("SansSerif", Font.BOLD, 18));
            check.setForeground(AppConfig.ACCENT_COLOR);

            JLabel txt = new JLabel(feat);
            txt.setFont(new Font("SansSerif", Font.PLAIN, 13));
            txt.setForeground(new Color(0xE2E8F0));

            fRow.add(check);
            fRow.add(txt);
            heroContent.add(fRow);
        }

        heroPanel.add(heroContent, BorderLayout.CENTER);

        // Footer version
        JLabel versionLabel = new JLabel("CampusFlow v" + AppConfig.VERSION + " • Java Swing SaaS");
        versionLabel.setFont(AppConfig.FONT_SMALL);
        versionLabel.setForeground(new Color(0x94A3B8));
        heroPanel.add(versionLabel, BorderLayout.SOUTH);

        rootPanel.add(heroPanel);

        // --- RIGHT LOGIN FORM PANEL ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new EmptyBorder(32, 40, 32, 40));

        JPanel formCard = new JPanel();
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(380, 520));

        // Welcome Header
        JLabel welcomeLabel = new JLabel("Welcome back");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        welcomeLabel.setForeground(AppConfig.TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(welcomeLabel);

        JLabel subLabel = new JLabel("Sign in with your campus credentials");
        subLabel.setFont(AppConfig.FONT_MEDIUM);
        subLabel.setForeground(AppConfig.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(subLabel);
        formCard.add(Box.createVerticalStrut(24));

        // Error message banner
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConfig.FONT_SMALL);
        errorLabel.setForeground(AppConfig.DANGER_COLOR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(errorLabel);
        formCard.add(Box.createVerticalStrut(10));

        // Username Field
        JLabel userLabel = new JLabel("Username or Email");
        userLabel.setFont(AppConfig.FONT_LABEL);
        userLabel.setForeground(AppConfig.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(userLabel);
        formCard.add(Box.createVerticalStrut(6));

        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(16));

        // Password Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(AppConfig.FONT_LABEL);
        passLabel.setForeground(AppConfig.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));

        // Password wrapper with Toggle button
        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setOpaque(false);
        passWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        passwordField.setPreferredSize(new Dimension(320, 42));

        togglePasswordBtn = new JButton("Show");
        togglePasswordBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        togglePasswordBtn.setForeground(AppConfig.TEXT_SECONDARY);
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setBorderPainted(false);
        togglePasswordBtn.setContentAreaFilled(false);
        togglePasswordBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePasswordBtn.addActionListener(e -> togglePasswordVisibility());

        passWrapper.add(passwordField, BorderLayout.CENTER);
        passWrapper.add(togglePasswordBtn, BorderLayout.EAST);
        formCard.add(passWrapper);
        formCard.add(Box.createVerticalStrut(22));

        // Sign In Button
        signInBtn = new ModernButton("Sign In to Portal", AppConfig.PRIMARY_COLOR, Color.WHITE, 10);
        signInBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.addActionListener(e -> triggerLogin());
        formCard.add(signInBtn);
        formCard.add(Box.createVerticalStrut(24));

        // ENTER key submission
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerLogin();
                }
            }
        };
        usernameField.addKeyListener(enterSubmit);
        passwordField.addKeyListener(enterSubmit);

        formContainer.add(formCard);
        rootPanel.add(formContainer);

        setContentPane(rootPanel);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(320, 42));
        return field;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            togglePasswordBtn.setText("Hide");
        } else {
            passwordField.setEchoChar('\u2022');
            togglePasswordBtn.setText("Show");
        }
    }

    private void triggerLogin() {
        clearError();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocusInWindow();
            return;
        }

        if (loginListener != null) {
            loginListener.onLogin(username, password);
        }
    }

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    public void showError(String message) {
        errorLabel.setText(message != null ? message : "");
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
    }

    public void setLoading(boolean loading) {
        signInBtn.setEnabled(!loading);
        signInBtn.setText(loading ? "Authenticating..." : "Sign In to Portal");
    }
}
