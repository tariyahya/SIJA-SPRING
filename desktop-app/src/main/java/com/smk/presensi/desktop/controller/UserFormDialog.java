package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Dialog controller untuk Add/Edit User
 */
public class UserFormDialog extends Dialog<User> {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField namaField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> tipeComboBox;
    @FXML private TextField rfidCardIdField;
    @FXML private CheckBox enabledCheckBox;
    @FXML private VBox passwordBox;
    
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label namaError;
    @FXML private Label roleError;
    @FXML private Label tipeError;
    
    private User existingUser;
    private boolean isEditMode;
    
    /**
     * Constructor untuk Add User (new)
     */
    public UserFormDialog() {
        this(null);
    }
    
    /**
     * Constructor untuk Edit User (existing)
     */
    public UserFormDialog(User user) {
        this.existingUser = user;
        this.isEditMode = (user != null);
        
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/user-form-dialog.fxml")
            );
            loader.setController(this);
            
            DialogPane dialogPane = loader.load();
            setDialogPane(dialogPane);
            
            setTitle(isEditMode ? "Edit User" : "Add New User");
            
            // Initialize after FXML loaded
            initialize();
            
            // Set result converter
            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    if (validateForm()) {
                        return buildUser();
                    }
                    // If validation fails, keep dialog open
                    return null;
                }
                return null;
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load user form dialog", e);
        }
    }
    
    private void initialize() {
        // Hide password field in edit mode
        if (isEditMode) {
            passwordBox.setVisible(false);
            passwordBox.setManaged(false);
        }
        
        // Pre-fill form if editing
        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            emailField.setText(existingUser.getEmail());
            namaField.setText(existingUser.getNama());
            roleComboBox.setValue(existingUser.getRole());
            tipeComboBox.setValue(existingUser.getTipe());
            rfidCardIdField.setText(existingUser.getRfidCardId());
            enabledCheckBox.setSelected(existingUser.getEnabled() != null ? existingUser.getEnabled() : true);
            
            // Username tidak bisa diubah saat edit
            usernameField.setDisable(true);
        } else {
            // Default values for new user
            roleComboBox.setValue("USER");
            tipeComboBox.setValue("SISWA");
            enabledCheckBox.setSelected(true);
        }
        
        // Add validation listeners
        usernameField.textProperty().addListener((obs, old, newVal) -> clearError(usernameError));
        emailField.textProperty().addListener((obs, old, newVal) -> clearError(emailError));
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError(passwordError));
        namaField.textProperty().addListener((obs, old, newVal) -> clearError(namaError));
        roleComboBox.valueProperty().addListener((obs, old, newVal) -> clearError(roleError));
        tipeComboBox.valueProperty().addListener((obs, old, newVal) -> clearError(tipeError));
    }
    
    private boolean validateForm() {
        boolean valid = true;
        
        // Clear all errors first
        clearAllErrors();
        
        // Validate username (required, min 3 chars) - only for new user
        if (!isEditMode) {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                showError(usernameError, "Username is required");
                valid = false;
            } else if (username.length() < 3) {
                showError(usernameError, "Username must be at least 3 characters");
                valid = false;
            }
        }
        
        // Validate email (required, valid format)
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailError, "Email is required");
            valid = false;
        } else if (!isValidEmail(email)) {
            showError(emailError, "Invalid email format");
            valid = false;
        }
        
        // Validate password (required for new user, min 6 chars)
        if (!isEditMode) {
            String password = passwordField.getText();
            if (password.isEmpty()) {
                showError(passwordError, "Password is required");
                valid = false;
            } else if (password.length() < 6) {
                showError(passwordError, "Password must be at least 6 characters");
                valid = false;
            }
        }
        
        // Validate nama (required)
        String nama = namaField.getText().trim();
        if (nama.isEmpty()) {
            showError(namaError, "Nama is required");
            valid = false;
        }
        
        // Validate role (required)
        if (roleComboBox.getValue() == null) {
            showError(roleError, "Role is required");
            valid = false;
        }
        
        // Validate tipe (required)
        if (tipeComboBox.getValue() == null) {
            showError(tipeError, "Tipe is required");
            valid = false;
        }
        
        return valid;
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private User buildUser() {
        User user = isEditMode ? existingUser : new User();
        
        // Only set username for new user
        if (!isEditMode) {
            user.setUsername(usernameField.getText().trim());
        }
        
        user.setEmail(emailField.getText().trim());
        user.setNama(namaField.getText().trim());
        user.setRole(roleComboBox.getValue());
        user.setTipe(tipeComboBox.getValue());
        user.setRfidCardId(rfidCardIdField.getText().trim());
        user.setEnabled(enabledCheckBox.isSelected());
        
        // Set password only for new user (backend will hash it)
        if (!isEditMode) {
            user.setPassword(passwordField.getText());
        }
        
        return user;
    }
    
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void clearAllErrors() {
        clearError(usernameError);
        clearError(emailError);
        clearError(passwordError);
        clearError(namaError);
        clearError(roleError);
        clearError(tipeError);
    }
}
