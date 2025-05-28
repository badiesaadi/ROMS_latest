import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CategoryButtonController {
    @FXML
    private VBox categoryButtonContainer;
    
    @FXML
    private ImageView categoryIcon;
    
    @FXML
    private Label categoryLabel;
    
    @FXML
    private TextField categoryNameField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button updateButton;
    
    private Runnable onClickHandler;
    
    private DatabaseConnection dbConnection = new DatabaseConnection();
    
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
            System.out.println("Error loading icon: " + e.getMessage());
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
    
    @FXML
    private void addCategory() {
        String categoryName = categoryNameField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("Category name cannot be empty");
            return;
        }

        Category category = new Category(categoryName);
        String query = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, category.getName());
            pstmt.executeUpdate();
            System.out.println("Category added: " + category.getName());
            categoryNameField.clear(); // Clear the input field after adding
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteCategory() {
        String categoryName = categoryNameField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("Category name cannot be empty");
            return;
        }

        String query = "DELETE FROM categories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Category deleted: " + categoryName);
            } else {
                System.out.println("Category not found: " + categoryName);
            }
            categoryNameField.clear(); // Clear the input field after deleting
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }
    
    @FXML
    private void updateCategory() {
        String categoryName = categoryNameField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("Category name cannot be empty");
            return;
        }

        // For updating, you need the old name to identify the category to update
        // For simplicity, let's assume the current label text is the old name
        String oldName = categoryLabel.getText();
        String query = "UPDATE categories SET name = ? WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            pstmt.setString(2, oldName);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Category updated from " + oldName + " to " + categoryName);
                categoryLabel.setText(categoryName); // Update the label to reflect the new name
            } else {
                System.out.println("Category not found: " + oldName);
            }
            categoryNameField.clear(); // Clear the input field after updating
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
        }
    }
}