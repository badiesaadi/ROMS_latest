import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import java.io.IOException;

public class WelcomeController {
    @FXML
    private Button startButton;

    @FXML
    private void handleStart(ActionEvent event) {
        try {
            Parent nextPage = FXMLLoader.load(getClass().getResource("customer_view.fxml"));
            Scene nextScene = new Scene(nextPage);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(nextScene);
            currentStage.setWidth(1100);
            currentStage.setHeight(800);
            StageManager.applyStageSettings(currentStage);
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFeedback(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("feedback_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Share Your Experience");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewReviews(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("feedback_display.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Customer Reviews");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading feedback display: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}