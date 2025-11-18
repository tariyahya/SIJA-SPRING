package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.User;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Controller untuk User Management View
 */
public class UserManagementController implements Initializable {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> tipeColumn;
    @FXML private TableColumn<User, String> rfidCardIdColumn;
    @FXML private TableColumn<User, Boolean> enabledColumn;
    @FXML private TableColumn<User, LocalDateTime> createdAtColumn;

    @FXML private TextField searchField;
    @FXML private Button addUserBtn;
    @FXML private Button editUserBtn;
    @FXML private Button deleteUserBtn;
    @FXML private Label statusLabel;
    @FXML private Label totalUsersLabel;

    private ObservableList<User> usersList;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize API client with singleton (shares JWT token)
        sessionManager = new SessionManager();
        apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }
        
        // Initialize UserService
        userService = new UserService(apiClient);

        // Setup table columns
        setupTableColumns();

        // Initialize data
        usersList = FXCollections.observableArrayList();
        usersTable.setItems(usersList);

        // Setup event handlers
        setupEventHandlers();

        // Load users
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        tipeColumn.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        rfidCardIdColumn.setCellValueFactory(new PropertyValueFactory<>("rfidCardId"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Custom cell factory for enabled column
        enabledColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean enabled, boolean empty) {
                super.updateItem(enabled, empty);
                
                if (empty || enabled == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(enabled ? "✓ Yes" : "✗ No");
                    setStyle(enabled ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }

    private void setupEventHandlers() {
        // Enable/disable edit and delete buttons based on selection
        usersTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue != null;
                editUserBtn.setDisable(!hasSelection);
                deleteUserBtn.setDisable(!hasSelection);
            }
        );

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void loadUsers() {
        updateStatus("Loading users...");
        
        // Load from API in background thread
        new Thread(() -> {
            try {
                List<User> users = userService.getAllUsers();
                
                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    usersList.clear();
                    usersList.addAll(users);
                    updateTotalUsers();
                    updateStatus("Users loaded successfully");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    updateStatus("Error loading users: " + e.getMessage());
                    showError("Failed to load users", e.getMessage());
                });
            }
        }).start();
    }

    private void filterUsers(String searchText) {
        // TODO: Implement search filtering
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all users
            return;
        }
        
        // Filter logic will be implemented here
        // String lowerCaseFilter = searchText.toLowerCase();
    }

    @FXML
    private void handleAddUser() {
        UserFormDialog dialog = new UserFormDialog();
        dialog.showAndWait().ifPresent(newUser -> {
            updateStatus("Creating user...");
            
            // Create via API in background thread
            new Thread(() -> {
                try {
                    User createdUser = userService.createUser(newUser);
                    
                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        usersList.add(createdUser);
                        updateTotalUsers();
                        updateStatus("User created: " + createdUser.getUsername());
                        showInfo("Success", "User created successfully!");
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("Failed to create user");
                        showError("Error", "Failed to create user: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            UserFormDialog dialog = new UserFormDialog(selectedUser);
            dialog.showAndWait().ifPresent(updatedUser -> {
                updateStatus("Updating user...");
                
                // Update via API in background thread
                new Thread(() -> {
                    try {
                        User result = userService.updateUser(updatedUser.getId(), updatedUser);
                        
                        // Update UI on JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> {
                            // Update the user in the list
                            int index = usersList.indexOf(selectedUser);
                            if (index >= 0) {
                                usersList.set(index, result);
                            }
                            updateStatus("User updated: " + result.getUsername());
                            showInfo("Success", "User updated successfully!");
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            updateStatus("Failed to update user");
                            showError("Error", "Failed to update user: " + e.getMessage());
                        });
                    }
                }).start();
            });
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Confirm delete
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Konfirmasi Delete");
            alert.setContentText("Apakah Anda yakin ingin menghapus user: " + selectedUser.getUsername() + "?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    updateStatus("Deleting user...");
                    
                    // Delete via API in background thread
                    new Thread(() -> {
                        try {
                            userService.deleteUser(selectedUser.getId());
                            
                            // Update UI on JavaFX Application Thread
                            javafx.application.Platform.runLater(() -> {
                                usersList.remove(selectedUser);
                                updateTotalUsers();
                                updateStatus("User deleted: " + selectedUser.getUsername());
                                showInfo("Success", "User deleted successfully!");
                            });
                            
                        } catch (Exception e) {
                            javafx.application.Platform.runLater(() -> {
                                updateStatus("Error deleting user");
                                showError("Delete Failed", "Failed to delete user: " + e.getMessage());
                            });
                        }
                    }).start();
                }
            });
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void updateTotalUsers() {
        if (totalUsersLabel != null) {
            totalUsersLabel.setText("Total Users: " + usersList.size());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
