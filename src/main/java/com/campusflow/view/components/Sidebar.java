package com.campusflow.view.components;

import com.campusflow.config.AppConfig;
import com.campusflow.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise navigation sidebar with clean typography, active pill indicator,
 * and role-based navigation without emojis.
 */
public class Sidebar extends JPanel {
    private static final long serialVersionUID = 1L;

    public interface NavigationListener {
        void onNavigate(String itemKey);
    }

    private final User.Role role;
    private final List<NavItemButton> navButtons = new ArrayList<>();
    private NavigationListener navigationListener;
    private Runnable logoutCallback;
    private String activeItem = "Dashboard";

    public Sidebar(User.Role role, Runnable logoutCallback) {
        this.role = role;
        this.logoutCallback = logoutCallback;

        setPreferredSize(new Dimension(250, 0));
        setBackground(AppConfig.SIDEBAR_BG);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // --- TOP BRANDING HEADER ---
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(26, 18, 22, 18));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);

        // Logo icon badge
        JPanel logoBadge = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(32, 32));
        logoBadge.setLayout(new GridBagLayout());
        JLabel logoSymbol = new JLabel("CF");
        logoSymbol.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoSymbol.setForeground(Color.WHITE);
        logoBadge.add(logoSymbol);
        brandRow.add(logoBadge);

        JLabel brandTitle = new JLabel(AppConfig.APP_NAME);
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
        brandTitle.setForeground(Color.WHITE);
        brandRow.add(brandTitle);

        headerPanel.add(brandRow);
        headerPanel.add(Box.createVerticalStrut(6));

        JLabel brandSubtitle = new JLabel("COLLEGE MANAGEMENT SYSTEM");
        brandSubtitle.setFont(new Font("SansSerif", Font.BOLD, 8));
        brandSubtitle.setForeground(new Color(0x94A3B8));
        brandSubtitle.setBorder(new EmptyBorder(0, 4, 0, 0));
        headerPanel.add(brandSubtitle);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x334E68));
        sep.setMaximumSize(new Dimension(200, 1));
        headerPanel.add(Box.createVerticalStrut(16));
        headerPanel.add(sep);

        add(headerPanel, BorderLayout.NORTH);

        // --- CENTER NAVIGATION ITEMS ---
        JPanel navListPanel = new JPanel();
        navListPanel.setOpaque(false);
        navListPanel.setLayout(new BoxLayout(navListPanel, BoxLayout.Y_AXIS));
        navListPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        String[] items = getNavItemsForRole(role);
        for (String itemKey : items) {
            NavItemButton btn = new NavItemButton(itemKey);
            navButtons.add(btn);
            navListPanel.add(btn);
            navListPanel.add(Box.createVerticalStrut(4));
        }

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(navListPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        // --- BOTTOM LOGOUT SECTION ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(16, 14, 24, 14));

        JSeparator bottomSep = new JSeparator();
        bottomSep.setForeground(new Color(0x334E68));
        bottomPanel.add(bottomSep);
        bottomPanel.add(Box.createVerticalStrut(14));

        ModernButton logoutBtn = new ModernButton(
                "Sign Out",
                new Color(0x2D3748),
                new Color(0xFEB2B2),
                8
        );
        logoutBtn.setFont(AppConfig.FONT_MEDIUM);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.addActionListener(e -> {
            if (logoutCallback != null) {
                logoutCallback.run();
            }
        });
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        setActiveItem("Dashboard");
    }

    private String[] getNavItemsForRole(User.Role r) {
        if (r == null) return new String[]{"Dashboard"};
        return switch (r) {
            case ADMIN -> new String[]{
                    "Dashboard",
                    "Students",
                    "Faculty",
                    "Courses",
                    "Enrollments",
                    "Attendance",
                    "Reports",
                    "Settings"
            };
            case FACULTY -> new String[]{
                    "Dashboard",
                    "My Courses",
                    "Attendance",
                    "Students",
                    "Grades"
            };
            case STUDENT -> new String[]{
                    "Dashboard",
                    "My Courses",
                    "Attendance",
                    "Schedule",
                    "Profile"
            };
        };
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setActiveItem(String itemKey) {
        this.activeItem = itemKey;
        for (NavItemButton btn : navButtons) {
            btn.setActive(btn.getItemKey().equalsIgnoreCase(itemKey));
        }
        repaint();
    }

    public String getActiveItem() {
        return activeItem;
    }

    /**
     * Inner navigation button component with active left accent bar and hover state.
     */
    private class NavItemButton extends JPanel {
        private static final long serialVersionUID = 1L;
        private final String itemKey;
        private boolean active = false;
        private boolean hovered = false;
        private final JLabel label;

        public NavItemButton(String itemKey) {
            this(itemKey, "");
        }

        public NavItemButton(String itemKey, String iconTextIgnored) {
            this.itemKey = itemKey;

            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 16, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            label = new JLabel(itemKey);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(AppConfig.SIDEBAR_TEXT);

            add(label);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    setActiveItem(itemKey);
                    if (navigationListener != null) {
                        navigationListener.onNavigate(itemKey);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 14));
            label.setForeground(active ? Color.WHITE : (hovered ? new Color(0xF1F5F9) : AppConfig.SIDEBAR_TEXT));
            repaint();
        }

        public String getItemKey() {
            return itemKey;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Background fill
                g2.setColor(new Color(0x334155));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Active indicator pill on left edge
                g2.setColor(AppConfig.ACCENT_COLOR);
                g2.fillRoundRect(2, 6, 4, getHeight() - 12, 4, 4);
            } else if (hovered) {
                g2.setColor(new Color(0x1E293B));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
