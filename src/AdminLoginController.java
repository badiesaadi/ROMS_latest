 
import java.io.IOException;
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
    @FXML private RadioButton adminRadio;
    @FXML private RadioButton kitchenRadio;
    @FXML private ToggleGroup loginType;
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Simple hardcoded authentication
        // Admin: username: admin, password: admin
        // Kitchen: username: kitchen, password: kitchen
        
        boolean isAdmin = adminRadio.isSelected();
        
        if (isAdmin && username.equals("admin") && password.equals("admin")) {
            loadDashboard("admin_dashboard.fxml", "Restaurant Admin Dashboard");
        } else if (!isAdmin && username.equals("kitchen") && password.equals("kitchen")) {
            loadDashboard("kitchen_dashboard.fxml", "Kitchen Dashboard");
        } else {
            errorLabel.setText("Invalid username or password");
            errorLabel.setVisible(true);
        }
    }
    
    private void loadDashboard(String fxmlFile, String title) {
        try {
            // Load the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
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
} 