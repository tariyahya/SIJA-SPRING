package com.smk.presensi.desktop.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.util.Duration;

/**
 * Utility class for showing in-app toast notifications
 */
public class InAppNotification {
    
    /**
     * Show notification toast at bottom of parent
     */
    public static void show(String message, Parent root) {
        show(message, root, NotificationType.INFO, 3);
    }

    /**
     * Show notification with type and custom duration
     */
    public static void show(String message, Parent root, NotificationType type, int durationSeconds) {
        if (!(root instanceof Pane)) {
            System.err.println("Root must be a Pane to show notification");
            return;
        }

        Pane pane = (Pane) root;

        // Create notification box
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.setStyle(getStyleForType(type));

        // Icon label
        Label iconLabel = new Label(getIconForType(type));
        iconLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");

        // Message label
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        // Close button
        Button closeBtn = new Button("×");
        closeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 24; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 0 5;"
        );

        notification.getChildren().addAll(iconLabel, messageLabel, closeBtn);

        // Position at bottom center
        notification.setLayoutX((pane.getWidth() - 500) / 2);
        notification.setLayoutY(pane.getHeight() - 100);

        // Make it responsive to window resize
        pane.widthProperty().addListener((obs, oldVal, newVal) -> {
            notification.setLayoutX((newVal.doubleValue() - 500) / 2);
        });

        pane.heightProperty().addListener((obs, oldVal, newVal) -> {
            notification.setLayoutY(newVal.doubleValue() - 100);
        });

        // Add to root
        pane.getChildren().add(notification);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Auto-hide after duration
        PauseTransition pause = new PauseTransition(Duration.seconds(durationSeconds));
        pause.setOnFinished(e -> hideNotification(notification, pane));
        pause.play();

        // Close button action
        closeBtn.setOnAction(e -> {
            pause.stop();
            hideNotification(notification, pane);
        });
    }

    private static void hideNotification(HBox notification, Pane pane) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> pane.getChildren().remove(notification));
        fadeOut.play();
    }

    private static String getStyleForType(NotificationType type) {
        String baseStyle = 
            "-fx-background-color: %s; " +
            "-fx-padding: 15 20; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2); " +
            "-fx-min-width: 450; " +
            "-fx-max-width: 500;";

        return String.format(baseStyle, switch (type) {
            case SUCCESS -> "#4CAF50"; // Green
            case ERROR -> "#f44336";   // Red
            case WARNING -> "#FF9800"; // Orange
            case INFO -> "#2196F3";    // Blue
        });
    }

    private static String getIconForType(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "✓";
            case ERROR -> "✗";
            case WARNING -> "⚠";
            case INFO -> "ℹ";
        };
    }

    public enum NotificationType {
        SUCCESS, ERROR, WARNING, INFO
    }
}
