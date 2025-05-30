import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML 
    private Button loginButton;

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = "manager"; // first user of app will be the super-user

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("All fields are required.");
            statusLabel.setVisible(true);
            return;
        }

        UsersTableDAO usersTableDAO = new UsersTableDAO();
        boolean isAdded = usersTableDAO.addUser(username, password, role);

        if (isAdded) {
            statusLabel.setText("Account created successfully.");
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setVisible(true);
        } else {
            statusLabel.setText("Error: Unable to create account.");
            statusLabel.setVisible(true);
        }
    }
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Log in");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}