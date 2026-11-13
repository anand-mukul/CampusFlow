package com.campusflow.view.components;

import com.campusflow.config.AppConfig;
import com.campusflow.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Top navigation bar displaying the current view title, system date,
 * role badge pill, and an avatar circle with the user's initials.
 */
public class TopBar extends JPanel {
    private final JLabel titleLabel;
    private final JLabel dateLabel;
    private final JLabel roleBadge;
    private final JLabel userNameLabel;
    private final JPanel avatarCircle;
    private final JLabel initialsLabel;
    private User.Role role;

    public TopBar(String pageTitle, User user) {
        this(
                pageTitle,
                user != null ? user.getUsername() : "Guest",
                user != null ? user.getRole() : User.Role.STUDENT
        );
    }

    public TopBar(String pageTitle, String displayName, User.Role role) {
        this.role = role;

        setPreferredSize(new Dimension(0, 68));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 28, 12, 28));

        // --- LEFT: PAGE TITLE ---
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(pageTitle);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLabel.setForeground(AppConfig.TEXT_PRIMARY);

        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(AppConfig.FONT_SMALL);
        dateLabel.setForeground(AppConfig.TEXT_SECONDARY);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(dateLabel);

        add(leftPanel, BorderLayout.WEST);

        // --- RIGHT: ROLE BADGE + USER INFO + AVATAR ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        // Role badge pill
        roleBadge = new JLabel(this.role != null ? this.role.name() : "GUEST") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color badgeBg = getRoleBadgeColor(TopBar.this.role);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roleBadge.setOpaque(false);
        roleBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        rightPanel.add(roleBadge);

        // User info text
        JPanel userMetaPanel = new JPanel();
        userMetaPanel.setOpaque(false);
        userMetaPanel.setLayout(new BoxLayout(userMetaPanel, BoxLayout.Y_AXIS));

        userNameLabel = new JLabel(displayName);
        userNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userNameLabel.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel userStatus = new JLabel("Online");
        userStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        userStatus.setForeground(AppConfig.SUCCESS_COLOR);

        userMetaPanel.add(userNameLabel);
        userMetaPanel.add(userStatus);
        rightPanel.add(userMetaPanel);

        // Avatar circle
        String initials = computeInitials(displayName);
        avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.PRIMARY_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(40, 40));
        avatarCircle.setLayout(new GridBagLayout());

        initialsLabel = new JLabel(initials);
        initialsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        initialsLabel.setForeground(Color.WHITE);
        avatarCircle.add(initialsLabel);

        rightPanel.add(avatarCircle);
        add(rightPanel, BorderLayout.EAST);
    }

    private Color getRoleBadgeColor(User.Role r) {
        if (r == null) return AppConfig.TEXT_SECONDARY;
        return switch (r) {
            case ADMIN -> new Color(0x4338CA); // Deep indigo
            case FACULTY -> new Color(0x0D9488); // Teal
            case STUDENT -> new Color(0x0284C7); // Vibrant Sky Blue
        };
    }

    private String computeInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public void setPageTitle(String title) {
        titleLabel.setText(title);
    }

    public void updateUserInfo(String name, User.Role newRole) {
        this.role = newRole;
        userNameLabel.setText(name);
        roleBadge.setText(newRole != null ? newRole.name() : "GUEST");
        initialsLabel.setText(computeInitials(name));
        roleBadge.repaint();
        repaint();
    }

    public User.Role getRole() {
        return role;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw subtle bottom border line
        g.setColor(new Color(0xE2E8F0));
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
