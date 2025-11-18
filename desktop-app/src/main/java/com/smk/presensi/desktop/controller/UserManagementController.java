package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.User;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize API client
        sessionManager = new SessionManager();
        apiClient = new ApiClient();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }

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
        // TODO: Load from API
        // For now, use mock data
        updateStatus("Loading users...");
        
        usersList.clear();
        
        // Mock data
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setEmail("admin@sija.com");
        user1.setRole("ADMIN");
        user1.setTipe("GURU");
        user1.setRfidCardId("ADMIN001");
        user1.setEnabled(true);
        user1.setCreatedAt(LocalDateTime.now().minusDays(10));
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("guru01");
        user2.setEmail("guru01@sija.com");
        user2.setRole("USER");
        user2.setTipe("GURU");
        user2.setRfidCardId("GURU001");
        user2.setEnabled(true);
        user2.setCreatedAt(LocalDateTime.now().minusDays(5));
        
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("siswa01");
        user3.setEmail("siswa01@sija.com");
        user3.setRole("USER");
        user3.setTipe("SISWA");
        user3.setRfidCardId("SISWA001");
        user3.setEnabled(true);
        user3.setCreatedAt(LocalDateTime.now().minusDays(3));
        
        usersList.addAll(user1, user2, user3);
        
        updateTotalUsers();
        updateStatus("Users loaded successfully");
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
        // TODO: Open Add User dialog
        showInfo("Add User", "Add User dialog akan diimplementasikan.");
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // TODO: Open Edit User dialog
            showInfo("Edit User", "Edit User dialog akan diimplementasikan untuk: " + selectedUser.getUsername());
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
                    // TODO: Delete via API
                    usersList.remove(selectedUser);
                    updateTotalUsers();
                    updateStatus("User deleted: " + selectedUser.getUsername());
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
}
