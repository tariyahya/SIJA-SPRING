package com.smk.presensi.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main Application Class untuk Desktop App
 * Entry point untuk JavaFX Application
 */
public class DesktopApp extends Application {
    
    private static final String APP_TITLE = "SIJA Presensi - Dashboard Admin";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load Login Screen first
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();

            // Configure Stage
            primaryStage.setTitle("SIJA Presensi - Login");
            Scene scene = new Scene(root, 500, 650);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(500);
            primaryStage.setMinHeight(650);
            // Allow user to resize / maximize window
            primaryStage.setResizable(true);
            
            // Show window
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Cleanup resources when app closes
        System.out.println("Application closing...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
