package com.campusflow.view.components;

import com.campusflow.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A sleek, modern JButton featuring rounded corners, subtle hover/pressed
 * animations, and custom styling following the CampusFlow design system.
 */
public class ModernButton extends JButton {
    private Color normalBackground;
    private Color hoverBackground;
    private Color pressedBackground;
    private Color textColor;
    private int cornerRadius;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text) {
        this(text, AppConfig.PRIMARY_COLOR, Color.WHITE, 10);
    }

    public ModernButton(String text, Color bgColor, Color fgColor) {
        this(text, bgColor, fgColor, 10);
    }

    public ModernButton(String text, Color bgColor, Color fgColor, int radius) {
        super(text);
        this.normalBackground = bgColor;
        this.textColor = fgColor;
        this.cornerRadius = radius;
        this.hoverBackground = computeHoverColor(bgColor);
        this.pressedBackground = computePressedColor(bgColor);

        setFont(AppConfig.FONT_MEDIUM);
        setForeground(fgColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setMargin(new Insets(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public static ModernButton primary(String text) {
        return new ModernButton(text, AppConfig.PRIMARY_COLOR, Color.WHITE);
    }

    public static ModernButton secondary(String text) {
        return new ModernButton(text, AppConfig.SECONDARY_COLOR, Color.WHITE);
    }

    public static ModernButton accent(String text) {
        return new ModernButton(text, AppConfig.ACCENT_COLOR, Color.WHITE);
    }

    public static ModernButton danger(String text) {
        return new ModernButton(text, AppConfig.DANGER_COLOR, Color.WHITE);
    }

    public static ModernButton success(String text) {
        return new ModernButton(text, AppConfig.SUCCESS_COLOR, Color.WHITE);
    }

    public static ModernButton outline(String text, Color color) {
        ModernButton btn = new ModernButton(text, new Color(0, 0, 0, 0), color, 10);
        btn.hoverBackground = new Color(color.getRed(), color.getGreen(), color.getBlue(), 30);
        btn.pressedBackground = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);
        return btn;
    }

    private Color computeHoverColor(Color base) {
        if (base.getAlpha() == 0) return new Color(0, 0, 0, 20);
        int r = Math.min(255, (int) (base.getRed() * 1.15));
        int g = Math.min(255, (int) (base.getGreen() * 1.15));
        int b = Math.min(255, (int) (base.getBlue() * 1.15));
        return new Color(r, g, b, base.getAlpha());
    }

    private Color computePressedColor(Color base) {
        if (base.getAlpha() == 0) return new Color(0, 0, 0, 40);
        int r = Math.max(0, (int) (base.getRed() * 0.85));
        int g = Math.max(0, (int) (base.getGreen() * 0.85));
        int b = Math.max(0, (int) (base.getBlue() * 0.85));
        return new Color(r, g, b, base.getAlpha());
    }

    public void setCustomColors(Color normal, Color hover, Color pressed) {
        this.normalBackground = normal;
        this.hoverBackground = hover;
        this.pressedBackground = pressed;
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg;
        if (!isEnabled()) {
            bg = new Color(0xCBD5E1);
        } else if (isPressed) {
            bg = pressedBackground;
        } else if (isHovered) {
            bg = hoverBackground;
        } else {
            bg = normalBackground;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Draw text
        FontMetrics fm = g2.getFontMetrics(getFont());
        String text = getText();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;

        g2.setFont(getFont());
        g2.setColor(isEnabled() ? textColor : new Color(0x94A3B8));
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
