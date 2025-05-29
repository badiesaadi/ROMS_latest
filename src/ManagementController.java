import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;

public class ManagementController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private CheckBox managerCheckBox;

    @FXML
    private CheckBox kitchenCheckBox;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    private UsersTableDAO usersTableDAO = new UsersTableDAO();

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        usersTableDAO.getAllUsers().forEach( user -> userList.add(new User(user.getId(), user.getUsername(), user.getRole())));
        userTable.setItems(userList);
    }

    @FXML
    public void handleAddUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = getSelectedRole();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(AlertType.WARNING, "Please fill in all fields.");
            return;
        }

        boolean success = usersTableDAO.addUser(username, password, role);
        if (success) {
            showAlert(AlertType.INFORMATION, "User added successfully.");
            loadUsers();
        } else {
            showAlert(AlertType.ERROR, "Failed to add user.");
        }
    }

    @FXML
    public void handleUpdateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = getSelectedRole();

        if (selectedUser == null) {
            showAlert(AlertType.WARNING, "Please select a user to update.");
            return;
        }

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(AlertType.WARNING, "Please fill in all fields.");
            return;
        }

        boolean success = usersTableDAO.updateUser(selectedUser.getId(), username, password, role);
        if (success) {
            showAlert(AlertType.INFORMATION, "User updated successfully.");
            loadUsers();
        } else {
            showAlert(AlertType.ERROR, "Failed to update user.");
        }
    }

    @FXML
    public void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            boolean success = usersTableDAO.deleteUser(selectedUser.getId());
            if (success) {
                showAlert(AlertType.INFORMATION, "User deleted successfully.");
                loadUsers();
            } else {
                showAlert(AlertType.ERROR, "Failed to delete user.");
            }
        } else {
            showAlert(AlertType.WARNING, "Please select a user to delete.");
        }
    }

    @FXML
    public void populateFields(MouseEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            usernameField.setText(selectedUser.getUsername());
            passwordField.setText(""); // Passwords are typically not displayed for security reasons
            if ("manager".equals(selectedUser.getRole())) {
                managerCheckBox.setSelected(true);
                kitchenCheckBox.setSelected(false);
            } else if ("kitchen".equals(selectedUser.getRole())) {
                kitchenCheckBox.setSelected(true);
                managerCheckBox.setSelected(false);
            }
        }
    }

    private void showAlert(AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getSelectedRole() {
        if (managerCheckBox.isSelected() && kitchenCheckBox.isSelected()) {
            showAlert(AlertType.WARNING, "Please select only one role.");
            return null;
        } else if (managerCheckBox.isSelected()) {
            return "manager";
        } else if (kitchenCheckBox.isSelected()) {
            return "kitchen";
        } else {
            return null;
        }
    }

    public static class User {
        private int id;
        private String username;
        private String role;

        public User(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}