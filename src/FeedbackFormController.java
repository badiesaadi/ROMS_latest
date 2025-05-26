import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class FeedbackFormController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea commentField;
    @FXML
    private ToggleButton star1;
    @FXML
    private ToggleButton star2;
    @FXML
    private ToggleButton star3;
    @FXML
    private ToggleButton star4;
    @FXML
    private ToggleButton star5;

    @FXML
    private SVGPath starPath1;
    @FXML
    private SVGPath starPath2;
    @FXML
    private SVGPath starPath3;
    @FXML
    private SVGPath starPath4;
    @FXML
    private SVGPath starPath5;

    private ToggleButton[] stars;
    private SVGPath[] starPaths;
    private int currentRating = 0;

    @FXML
    public void initialize() {
        // Initialize arrays for easier star management
        stars = new ToggleButton[] { star1, star2, star3, star4, star5 };
        starPaths = new SVGPath[] { starPath1, starPath2, starPath3, starPath4, starPath5 };

        // Set default rating to 5 stars
        currentRating = 5;
        updateStarSelection(5);

        // Add hover effects
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnMouseEntered(e -> handleStarHover(index + 1));
            stars[i].setOnMouseExited(e -> updateStarSelection(currentRating));
        }
    }

    @FXML
    private void handleStarClick(javafx.event.ActionEvent event) {
        ToggleButton clickedStar = (ToggleButton) event.getSource();
        int clickedRating = 0;

        for (int i = 0; i < stars.length; i++) {
            if (stars[i] == clickedStar) {
                clickedRating = i + 1;
                break;
            }
        }

        currentRating = clickedRating;
        updateStarSelection(currentRating);
    }

    private void handleStarHover(int rating) {
        for (int i = 0; i < stars.length; i++) {
            boolean selected = i < rating;
            stars[i].setSelected(selected);
            updateStarStyle(i, selected);
        }
    }

    private int getCurrentRating() {
        return currentRating;
    }

    private void updateStarSelection(int rating) {
        for (int i = 0; i < stars.length; i++) {
            boolean selected = i < rating;
            stars[i].setSelected(selected);
            updateStarStyle(i, selected);
        }
    }

    private void updateStarStyle(int index, boolean selected) {
        if (selected) {
            starPaths[index].setStyle("-fx-fill: #FFD700; -fx-stroke: #FFD700; -fx-stroke-width: 0.5;");
        } else {
            starPaths[index].setStyle("-fx-fill: #ddd; -fx-stroke: transparent; -fx-stroke-width: 0.5;");
        }
    }

    @FXML
    private void handleSubmit() {
        // Validate inputs
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter your name");
            return;
        }

        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter your phone number");
            return;
        }

        // Validate phone number format
        String phonePattern = "^\\+?[0-9]{10,}$";
        if (!Pattern.matches(phonePattern, phoneField.getText().trim())) {
            showAlert("Error", "Please enter a valid phone number");
            return;
        }

        if (commentField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter your feedback");
            return;
        }

        int rating = getCurrentRating();
        if (rating == 0) {
            showAlert("Error", "Please select a rating");
            return;
        }

        // Save feedback to database
        try {
            saveFeedback(
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    commentField.getText().trim(),
                    rating);

            showAlert("Success", "Thank you for your feedback!");

            // Clear form
            nameField.clear();
            phoneField.clear();
            commentField.clear();
            updateStarSelection(5); // Reset to 5 stars

        } catch (SQLException e) {
            showAlert("Error", "Failed to save feedback. Please try again later.");
            e.printStackTrace();
        }
    }

    private void saveFeedback(String name, String phone, String comment, int rating) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO feedback (name, phone, comment, rating, submission_date) VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, comment);
            stmt.setInt(4, rating);
            stmt.setString(5, LocalDateTime.now().toString());

            stmt.executeUpdate();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}