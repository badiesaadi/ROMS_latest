import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class AdminLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Button signupButton;

    private String currentUserRole; //  to store the role of the logged-in user

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            errorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT role FROM users WHERE username = ? AND mot_de_pass = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    currentUserRole = rs.getString("role"); // Save the role   

                    if ("manager".equals(currentUserRole)) {
                        loadDashboard("admin_dashboard.fxml", "Restaurant Admin Dashboard");
                    } 
                    else if("sub_manager".equals(currentUserRole)){
                        loadDashboard("admin_dashboard.fxml", "Restaurant Admin Dashboard");
                    }
                    else if ("kitchen".equals(currentUserRole)) {
                        loadDashboard("kitchen_dashboard.fxml", "Kitchen Dashboard");
                    } else {
                        errorLabel.setText("Invalid role.");
                        errorLabel.setVisible(true);
                    }
                } else {
                    errorLabel.setText("Invalid username or password.");
                    errorLabel.setVisible(true);
                }
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    private void loadDashboard(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass the current user role to the AdminDashboardController
            //check this is causing problem with 
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setCurrentUserRole(currentUserRole);
            }

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setWidth(1500);
            StageManager.applyStageSettings(stage);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading dashboard: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToCustomerView(ActionEvent event) {
        try {
            // Load the customer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer_view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            StageManager.applyStageSettings(stage);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sign Up");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading signup page: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM users";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    signupButton.setVisible(true);
                }
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

     String getCurrentUserRole() {
        return currentUserRole; 
    }
}