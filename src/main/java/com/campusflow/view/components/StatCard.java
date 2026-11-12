package com.campusflow.view.components;

import com.campusflow.config.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A modern SaaS-style metric card displaying a KPI value, title, icon badge,
 * and contextual subtitle.
 */
public class StatCard extends RoundedPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final JPanel iconBadge;
    private final JLabel iconLabel;
    private Color accentColor;

    public StatCard(String title, String value, Color accentColor) {
        this(title, value, "", accentColor, "●");
    }

    public StatCard(String title, String value, String subtitle, Color accentColor, String iconText) {
        super(16, AppConfig.CARD_COLOR, new Color(0xE2E8F0), 1);
        this.accentColor = accentColor;

        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(18, 20, 18, 20));
        setPreferredSize(new Dimension(240, 110));

        // Left / Right Icon badge
        iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color badgeBg = new Color(
                        accentColor.getRed(),
                        accentColor.getGreen(),
                        accentColor.getBlue(),
                        35
                );
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(50, 50));
        iconBadge.setLayout(new GridBagLayout());

        iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setForeground(accentColor);
        iconBadge.add(iconLabel);

        // Center Content (Title, Value, Subtitle)
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(AppConfig.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(AppConfig.TEXT_PRIMARY);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(AppConfig.FONT_SMALL);
        subtitleLabel.setForeground(new Color(0x64748B));

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(valueLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            contentPanel.add(Box.createVerticalStrut(3));
            contentPanel.add(subtitleLabel);
        }

        add(contentPanel, BorderLayout.CENTER);
        add(iconBadge, BorderLayout.EAST);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setAccentColor(Color color) {
        this.accentColor = color;
        iconLabel.setForeground(color);
        iconBadge.repaint();
    }

    public Color getAccentColor() {
        return accentColor;
    }
}
