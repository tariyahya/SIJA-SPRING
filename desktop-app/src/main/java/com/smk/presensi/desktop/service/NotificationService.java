package com.smk.presensi.desktop.service;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.net.URL;

/**
 * Service for managing system tray notifications
 * Provides desktop notifications using AWT SystemTray API
 */
public class NotificationService {
    private static NotificationService instance;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private boolean initialized = false;

    private NotificationService() {
        setupSystemTray();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this platform");
            return;
        }

        try {
            tray = SystemTray.getSystemTray();

            // Load icon - try multiple paths
            Image icon = loadIcon();
            
            if (icon == null) {
                System.err.println("Failed to load tray icon, using default");
                return;
            }

            trayIcon = new TrayIcon(icon, "SIJA Desktop");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("SIJA Desktop App - Presensi System");

            // Add popup menu (optional)
            PopupMenu popup = new PopupMenu();
            
            MenuItem aboutItem = new MenuItem("About SIJA");
            aboutItem.addActionListener(e -> {
                showInfo("About", "SIJA Desktop v1.0.0\nSistem Informasi Jaringan dan Aplikasi");
            });
            
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                System.exit(0);
            });
            
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(exitItem);
            
            trayIcon.setPopupMenu(popup);

            // Add to tray
            tray.add(trayIcon);
            initialized = true;

            System.out.println("System tray initialized successfully");
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }

    private Image loadIcon() {
        // Try to load icon from resources
        try {
            URL iconUrl = getClass().getResource("/images/app-icon.png");
            if (iconUrl != null) {
                return Toolkit.getDefaultToolkit().getImage(iconUrl);
            }
        } catch (Exception e) {
            System.err.println("Failed to load icon from /images/app-icon.png");
        }

        // Fallback: create simple colored square
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(new Color(33, 150, 243)); // Material Blue
            g2d.fillRect(0, 0, 32, 32);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("S", 9, 23);
            g2d.dispose();
            return img;
        } catch (Exception e) {
            System.err.println("Failed to create fallback icon: " + e.getMessage());
            return null;
        }
    }

    /**
     * Show notification with specified type
     */
    public void showNotification(String title, String message, MessageType type) {
        if (!initialized || trayIcon == null) {
            System.out.println("Notification (not shown): " + title + " - " + message);
            return;
        }

        try {
            trayIcon.displayMessage(title, message, type);
        } catch (Exception e) {
            System.err.println("Failed to show notification: " + e.getMessage());
        }
    }

    /**
     * Show info notification
     */
    public void showInfo(String title, String message) {
        showNotification(title, message, MessageType.INFO);
    }

    /**
     * Show warning notification
     */
    public void showWarning(String title, String message) {
        showNotification(title, message, MessageType.WARNING);
    }

    /**
     * Show error notification
     */
    public void showError(String title, String message) {
        showNotification(title, message, MessageType.ERROR);
    }

    /**
     * Check if system tray is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Remove tray icon and cleanup
     */
    public void remove() {
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
            initialized = false;
            System.out.println("System tray removed");
        }
    }
}
