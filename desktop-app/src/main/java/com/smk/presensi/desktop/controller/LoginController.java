package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.viewmodel.LoginViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController - Handle login screen UI
 */
public class LoginController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private LoginViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize SessionManager & ViewModel with singleton ApiClient
        sessionManager = new SessionManager();
        ApiClient apiClient = ApiClient.getInstance();
        viewModel = new LoginViewModel(apiClient, sessionManager);

        // Bind UI to ViewModel
        bindUI();

        // Setup event handlers
        setupEventHandlers();

        // Try auto-login
        tryAutoLogin();
    }

    private void bindUI() {
        // Bind text fields
        usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        rememberMeCheckbox.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());

        // Bind loading state
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        loginButton.disableProperty().bind(viewModel.loadingProperty());

        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());

        // Listen for login success
        viewModel.loginSuccessProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                navigateToDashboard();
            }
        });
    }

    private void setupEventHandlers() {
        // Login button
        loginButton.setOnAction(event -> handleLogin());

        // Enter key in password field
        passwordField.setOnAction(event -> handleLogin());

        // Clear error saat user mulai ketik
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) viewModel.clearError();
        });
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) viewModel.clearError();
        });
    }

    @FXML
    private void handleLogin() {
        viewModel.login();
    }

    /**
     * Try auto-login dengan saved session
     */
    private void tryAutoLogin() {
        if (viewModel.autoLogin()) {
            navigateToDashboard();
        }
    }

    /**
     * Navigate ke Dashboard setelah login sukses
     */
    private void navigateToDashboard() {
        try {
            // Load dashboard FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dashboard.fxml")
            );
            Parent root = loader.load();

            // Get dashboard controller dan pass SessionManager
            DashboardController dashboardController = loader.getController();
            dashboardController.setSessionManager(sessionManager);

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("SIJA Presensi - Dashboard Admin");
            stage.setMinWidth(1024);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.setMaximized(true);

        } catch (IOException e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
