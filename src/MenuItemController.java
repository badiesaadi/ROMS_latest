import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class MenuItemController {
    @FXML
    private VBox menuCardContainer;

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuName;

    @FXML
    private Label menuPrice;

    @FXML
    private Spinner<Integer> addToOrder;

    @FXML
    private Button submitQuantityBtn;

    private MenuItem menuItem;
    private MenuItemDAO menuItemDAO;

    // Initialize the controller with a MenuItem
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.menuItemDAO = new MenuItemDAO();
        initializeUI();
    }

    @FXML
    public void initialize() {
        // Configure the Spinner (e.g., set min/max values)
        addToOrder.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));

        // Bind button action
        submitQuantityBtn.setOnAction(event -> updateMenuItemQuantity());
    }

    private void initializeUI() {
        if (menuItem != null) {
            menuName.setText(menuItem.getTitle());
            menuPrice.setText(String.format("$%.2f", menuItem.getPrice()));
            addToOrder.getValueFactory().setValue(menuItem.getQuantity());
            if (menuItem.getImagePath() != null && !menuItem.getImagePath().isEmpty()) {
                menuImage.setImage(new Image(menuItem.getImagePath()));
            }
        }
    }

    @FXML
    private void updateMenuItemQuantity() {
        if (menuItem == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No menu item selected.");
            return;
        }

        try {
            int newQuantity = addToOrder.getValue();
            menuItem.setQuantity(newQuantity);
            boolean success = menuItemDAO.updateMenuItem(menuItem);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item quantity updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update menu item. Item may not exist or category is invalid.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update menu item: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}