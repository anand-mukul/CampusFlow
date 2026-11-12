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
 * A sleek vertical navigation sidebar styled with a modern dark SaaS theme.
 * Supports role-based navigation links, active states, and a logout action.
 */
public class Sidebar extends JPanel {
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

        setPreferredSize(new Dimension(240, 0));
        setBackground(AppConfig.SIDEBAR_BG);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // --- TOP BRANDING HEADER ---
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(28, 22, 24, 22));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);

        // Logo icon badge
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(34, 34));
        logoBadge.setLayout(new GridBagLayout());
        JLabel logoSymbol = new JLabel("CF");
        logoSymbol.setFont(new Font("SansSerif", Font.BOLD, 15));
        logoSymbol.setForeground(Color.WHITE);
        logoBadge.add(logoSymbol);
        brandRow.add(logoBadge);

        JLabel brandTitle = new JLabel(AppConfig.APP_NAME);
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        brandTitle.setForeground(Color.WHITE);
        brandRow.add(brandTitle);

        headerPanel.add(brandRow);
        headerPanel.add(Box.createVerticalStrut(6));

        JLabel brandSubtitle = new JLabel("COLLEGE MANAGEMENT");
        brandSubtitle.setFont(new Font("SansSerif", Font.BOLD, 9));
        brandSubtitle.setForeground(new Color(0x94A3B8));
        brandSubtitle.setBorder(new EmptyBorder(0, 4, 0, 0));
        headerPanel.add(brandSubtitle);

        // Subtle separator
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
        navListPanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        String[][] items = getNavItemsForRole(role);
        for (String[] it : items) {
            String key = it[0];
            String icon = it[1];
            NavItemButton btn = new NavItemButton(key, icon);
            navButtons.add(btn);
            navListPanel.add(btn);
            navListPanel.add(Box.createVerticalStrut(6));
        }

        // Fill remaining space
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(navListPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        // --- BOTTOM USER & LOGOUT SECTION ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(16, 14, 24, 14));

        JSeparator bottomSep = new JSeparator();
        bottomSep.setForeground(new Color(0x334E68));
        bottomPanel.add(bottomSep);
        bottomPanel.add(Box.createVerticalStrut(14));

        ModernButton logoutBtn = new ModernButton(
                "⎋  Sign Out",
                new Color(0x2D3748),
                new Color(0xFEB2B2),
                10
        );
        logoutBtn.setFont(AppConfig.FONT_MEDIUM);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.addActionListener(e -> {
            if (logoutCallback != null) {
                logoutCallback.run();
            }
        });
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Mark default active item
        setActiveItem("Dashboard");
    }

    private String[][] getNavItemsForRole(User.Role r) {
        if (r == null) return new String[][]{{"Dashboard", "📊"}};
        return switch (r) {
            case ADMIN -> new String[][]{
                    {"Dashboard", "📊"},
                    {"Students", "🎓"},
                    {"Faculty", "👨‍🏫"},
                    {"Courses", "📚"},
                    {"Enrollments", "📋"},
                    {"Attendance", "✓"},
                    {"Reports", "📈"},
                    {"Settings", "⚙"}
            };
            case FACULTY -> new String[][]{
                    {"Dashboard", "📊"},
                    {"My Courses", "📚"},
                    {"Attendance", "✓"},
                    {"Students", "🎓"},
                    {"Grades", "📝"}
            };
            case STUDENT -> new String[][]{
                    {"Dashboard", "📊"},
                    {"My Courses", "📚"},
                    {"Attendance", "✓"},
                    {"Schedule", "📅"},
                    {"Profile", "👤"}
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
     * Inner navigation button component with active highlighting and hover animations.
     */
    private class NavItemButton extends JPanel {
        private final String itemKey;
        private boolean active = false;
        private boolean hovered = false;
        private final JLabel label;
        private final JLabel icon;

        public NavItemButton(String itemKey, String iconText) {
            this.itemKey = itemKey;

            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            icon = new JLabel(iconText);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            icon.setForeground(AppConfig.SIDEBAR_TEXT);

            label = new JLabel(itemKey);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(AppConfig.SIDEBAR_TEXT);

            add(icon);
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

        public String getItemKey() {
            return itemKey;
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                label.setForeground(Color.WHITE);
                label.setFont(new Font("SansSerif", Font.BOLD, 14));
                icon.setForeground(AppConfig.ACCENT_COLOR);
            } else {
                label.setForeground(AppConfig.SIDEBAR_TEXT);
                label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                icon.setForeground(AppConfig.SIDEBAR_TEXT);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (active) {
                // Active pill background
                g2.setColor(new Color(0x2D5986));
                g2.fillRoundRect(0, 0, width, height, 10, 10);

                // Left active accent indicator bar
                g2.setColor(AppConfig.ACCENT_COLOR);
                g2.fillRoundRect(0, 6, 4, height - 12, 4, 4);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, width, height, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
