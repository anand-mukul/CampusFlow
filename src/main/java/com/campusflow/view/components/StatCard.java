package com.campusflow.view.components;

import com.campusflow.config.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Enterprise SaaS KPI metric card.
 * Features an accent indicator bar, uppercase category label, prominent numerical display,
 * and contextual status subtitle with zero decorative emojis.
 */
public class StatCard extends RoundedPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private Color accentColor;

    public StatCard(String title, String value, Color accentColor) {
        this(title, value, "", accentColor, "");
    }

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this(title, value, subtitle, accentColor, "");
    }

    public StatCard(String title, String value, String subtitle, Color accentColor, String iconText) {
        super(14, AppConfig.CARD_COLOR, new Color(0xE2E8F0), 1);
        this.accentColor = accentColor != null ? accentColor : AppConfig.PRIMARY_COLOR;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setPreferredSize(new Dimension(240, 108));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title != null ? title.toUpperCase() : "");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(AppConfig.TEXT_SECONDARY);

        valueLabel = new JLabel(value != null ? value : "0");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(AppConfig.TEXT_PRIMARY);

        subtitleLabel = new JLabel(subtitle != null ? subtitle : "");
        subtitleLabel.setFont(AppConfig.FONT_SMALL);
        subtitleLabel.setForeground(new Color(0x64748B));

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(valueLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            contentPanel.add(Box.createVerticalStrut(4));
            contentPanel.add(subtitleLabel);
        }

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (accentColor != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(accentColor);
            // Draw clean 3px accent top bar
            g2.fillRoundRect(2, 2, getWidth() - 4, 3, 2, 2);
            g2.dispose();
        }
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public void setTitle(String title) {
        titleLabel.setText(title != null ? title.toUpperCase() : "");
    }

    public void setAccentColor(Color color) {
        this.accentColor = color;
        repaint();
    }

    public Color getAccentColor() {
        return accentColor;
    }
}
