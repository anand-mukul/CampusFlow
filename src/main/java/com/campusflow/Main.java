package com.campusflow;

import com.campusflow.config.AppConfig;
import com.campusflow.controller.AuthController;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for CampusFlow.
 * Sets up the FlatLaf modern UI theme, enables anti-aliasing, and launches
 * the authentication flow.
 */
public class Main {

    public static void main(String[] args) {
        // Enable anti-aliasing for text and geometry across all Swing components
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize FlatLaf modern Look & Feel
        try {
            FlatLightLaf.setup();

            // Set global UI defaults for a polished SaaS look
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Table.selectionBackground", new Color(0xE0F2FE));
            UIManager.put("Table.selectionForeground", AppConfig.TEXT_PRIMARY);
        } catch (Exception ex) {
            System.err.println("Could not initialize FlatLaf look and feel: " + ex.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // Launch Application on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AuthController authController = new AuthController();
            authController.start();
        });
    }
}
