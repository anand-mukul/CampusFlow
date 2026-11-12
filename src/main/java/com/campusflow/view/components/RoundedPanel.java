package com.campusflow.view.components;

import com.campusflow.config.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A modern JPanel with smooth rounded corners, anti-aliased rendering,
 * and optional border.
 */
public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private int borderThickness;

    public RoundedPanel() {
        this(16, AppConfig.CARD_COLOR, new Color(0xE2E8F0), 1);
    }

    public RoundedPanel(int radius) {
        this(radius, AppConfig.CARD_COLOR, null, 0);
    }

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, null, 0);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor) {
        this(radius, bgColor, borderColor, 1);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderThickness) {
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        setOpaque(false);
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // Draw background
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        }

        // Draw border if present
        if (borderColor != null && borderThickness > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            int offset = borderThickness / 2;
            g2.drawRoundRect(offset, offset, width - borderThickness, height - borderThickness, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
