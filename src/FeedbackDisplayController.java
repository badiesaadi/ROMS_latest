import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Pos;

public class FeedbackDisplayController {
    @FXML
    private VBox feedbackContainer;

    private List<Feedback> feedbackList = new ArrayList<>();
    private boolean sortByDate = true;

    @FXML
    private void initialize() {
        loadFeedback();
        displayFeedback();
    }

    private void loadFeedback() {
        String sql = "SELECT name, comment, rating, submission_date FROM feedback ORDER BY submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            feedbackList.clear();
            while (rs.next()) {
                Feedback feedback = new Feedback(
                        rs.getString("name"),
                        rs.getString("comment"),
                        rs.getInt("rating"),
                        rs.getTimestamp("submission_date").toLocalDateTime());
                feedbackList.add(feedback);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading feedback: " + e.getMessage());
        }
    }

    private void displayFeedback() {
        feedbackContainer.getChildren().clear();

        if (sortByDate) {
            feedbackList.sort(Comparator.comparing(Feedback::getDate).reversed());
        } else {
            feedbackList.sort(Comparator.comparing(Feedback::getRating).reversed());
        }

        for (Feedback feedback : feedbackList) {
            VBox card = createFeedbackCard(feedback);
            feedbackContainer.getChildren().add(card);
        }
    }

    private VBox createFeedbackCard(Feedback feedback) {
        VBox card = new VBox(10);
        card.getStyleClass().add("feedback-card");

        // Create HBox for name and rating
        HBox nameAndRating = new HBox(10);
        nameAndRating.setAlignment(Pos.CENTER_LEFT);

        // Name
        Label nameLabel = new Label(feedback.getName());
        nameLabel.getStyleClass().add("name");

        // Rating
        HBox ratingStars = new HBox(2);
        ratingStars.getStyleClass().add("rating");
        for (int i = 0; i < feedback.getRating(); i++) {
            SVGPath star = new SVGPath();
            star.setContent(
                    "M12 2 L15.09 8.26 L22 9.27 L17 14.14 L18.18 21.02 L12 17.77 L5.82 21.02 L7 14.14 L2 9.27 L8.91 8.26 L12 2");
            star.getStyleClass().add("svg-path");
            ratingStars.getChildren().add(star);
        }

        // Add name and rating to the HBox
        nameAndRating.getChildren().addAll(nameLabel, ratingStars);

        // Comment
        Text commentText = new Text(feedback.getComment());
        commentText.getStyleClass().add("feedback-comment");
        commentText.setWrappingWidth(700);

        // Date
        Label dateLabel = new Label(feedback.getDate().format(
                DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")));
        dateLabel.getStyleClass().add("date");

        card.getChildren().addAll(nameAndRating, commentText, dateLabel);
        return card;
    }

    @FXML
    private void sortByDate() {
        sortByDate = true;
        displayFeedback();
    }

    @FXML
    private void sortByRating() {
        sortByDate = false;
        displayFeedback();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) feedbackContainer.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class Feedback {
        private final String name;
        private final String comment;
        private final int rating;
        private final java.time.LocalDateTime date;

        public Feedback(String name, String comment, int rating, java.time.LocalDateTime date) {
            this.name = name;
            this.comment = comment;
            this.rating = rating;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public int getRating() {
            return rating;
        }

        public java.time.LocalDateTime getDate() {
            return date;
        }
    }
}