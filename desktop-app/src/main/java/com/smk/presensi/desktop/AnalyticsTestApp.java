package com.smk.presensi.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Test launcher for Analytics View
 * Run this to test the charts without running full app
 */
public class AnalyticsTestApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/analytics.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        
        primaryStage.setTitle("SIJA Desktop - Analytics Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
