import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class CategoryButtonController {
    @FXML
    private VBox categoryButtonContainer;

    @FXML
    private ImageView categoryIcon;

    @FXML
    private Label categoryLabel;

    private Runnable onClickHandler;
    
    @FXML
    private void initialize() {
        categoryButtonContainer.setOnMouseClicked(event -> {
            if (onClickHandler != null) {
                onClickHandler.run();
            }
        });

        // Prevent text from changing weight on focus/unfocus
        categoryLabel.setStyle("-fx-font-weight: normal;");
    }

    public void setCategory(String category, String iconPath) {
        categoryLabel.setText(category);

        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            categoryIcon.setImage(icon);
        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        }
    }

    public void setOnAction(Runnable handler) {
        this.onClickHandler = handler;
    }

    public void setActive(boolean active) {
        if (active) {
            categoryButtonContainer.setStyle("-fx-background-color: #FB9400;-fx-text-fill: white; -fx-background-radius: 8;");
            categoryLabel.setTextFill(Color.WHITE);
        } else {
            categoryButtonContainer.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
            categoryLabel.setTextFill(Color.BLACK);
        }

        // Set user data to track active state
        categoryButtonContainer.setUserData(active);
    }


}