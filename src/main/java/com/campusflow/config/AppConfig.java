package com.campusflow.config;

import java.awt.*;

public class AppConfig {
    public static final String APP_NAME = "CampusFlow";
    public static final String VERSION  = "1.0.0";

    // Color palette
    public static final Color PRIMARY_COLOR    = new Color(0x1E3A5F);
    public static final Color SECONDARY_COLOR  = new Color(0x2D5986);
    public static final Color ACCENT_COLOR     = new Color(0x00B4D8);
    public static final Color BACKGROUND_COLOR = new Color(0xF0F4F8);
    public static final Color CARD_COLOR       = Color.WHITE;
    public static final Color TEXT_PRIMARY     = new Color(0x1A202C);
    public static final Color TEXT_SECONDARY   = new Color(0x718096);
    public static final Color SUCCESS_COLOR    = new Color(0x38A169);
    public static final Color WARNING_COLOR    = new Color(0xD69E2E);
    public static final Color DANGER_COLOR     = new Color(0xE53E3E);
    public static final Color SIDEBAR_BG       = new Color(0x1E3A5F);
    public static final Color SIDEBAR_TEXT     = new Color(0xE2E8F0);
    public static final Color SIDEBAR_ACTIVE   = new Color(0x00B4D8);

    // Fonts
    public static final Font FONT_LARGE   = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_MEDIUM  = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_REGULAR = FONT_MEDIUM;
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD, 13);

    private AppConfig() {}
}

