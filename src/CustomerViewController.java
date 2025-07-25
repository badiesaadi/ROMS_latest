import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node; // Import Node class


public class CustomerViewController implements Initializable {
    @FXML
    private VBox cartItemsContainer;

    @FXML
    private FlowPane menuItemsContainer;
    @FXML
    private HBox menuTypesContainer;
    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button placeOrderBtn;


    private List<MenuItem> menuItems = new ArrayList<>();
    private Map<Integer, CartItem> cartItems = new HashMap<>();
    private Map<Integer, Spinner<Integer>> menuSpinners = new HashMap<>();
    private String currentCategory;

    private double total = 0.0;

    @Override
    public void initialize(URL location, @SuppressWarnings("unused") ResourceBundle resources) {
        loadMenuItems();
        setupSearch();
        setupCategoryButtons();
        currentCategory = "All"; // Ensure "All" is selected initially
        filterAndDisplayMenuItems(); // Sort and display items initially
    }


    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayMenuItems();
        });
    }

    private void setupCategoryButtons() {
        // Clear existing buttons
        menuTypesContainer.getChildren().clear();

        // Add "All" button
        FXMLLoader allLoader = new FXMLLoader(getClass().getResource("category_button.fxml"));
        try {
            VBox allButton = allLoader.load();
            CategoryButtonController allController = allLoader.getController();
            allController.setCategory("All", "/images/all-icon.png");
            allController.setOnAction(() -> {
                currentCategory = "All";
                activateCategoryButton(allController);
                filterAndDisplayMenuItems();
            });
            menuTypesContainer.getChildren().add(allButton);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load categories from database
        MenuItemDAO menuItemDAO = new MenuItemDAO();
        List<String> categories = menuItemDAO.getAllCategories();

        // Add category buttons
        for (String category : categories) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("category_button.fxml"));
            try {
                VBox categoryButton = loader.load();
                CategoryButtonController controller = loader.getController();
                controller.setCategory(category, "/images/" + category.toLowerCase() + "-icon.png");
                controller.setOnAction(() -> {
                    currentCategory = category;
                    activateCategoryButton(controller);
                    filterAndDisplayMenuItems();
                });
                menuTypesContainer.getChildren().add(categoryButton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void activateCategoryButton(CategoryButtonController activeController) {
        for (Node node : menuTypesContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                CategoryButtonController controller = (CategoryButtonController) vbox.getUserData();
                if (controller != null) {
                    controller.setActive(controller == activeController);
                }
            }
        }
    }


    private void displayMenuItems() {
        menuItemsContainer.getChildren().clear();
        for (MenuItem item : menuItems) {
            displayMenuItem(item);
        }
    }

    private void filterAndDisplayMenuItems() {
        String searchText = searchField.getText().toLowerCase();
        menuItemsContainer.getChildren().clear();

        List<MenuItem> filteredItems = new ArrayList<>();

        for (MenuItem item : menuItems) {
            if ((currentCategory.equals("All") || item.getCategory().equals(currentCategory)) &&
                    (searchText.isEmpty() || item.getName().toLowerCase().contains(searchText))) {
                filteredItems.add(item);
            }
        }

        if (currentCategory.equals("All")) {
            filteredItems.sort((item1, item2) -> item1.getCategory().compareTo(item2.getCategory()));
        }

        for (MenuItem item : filteredItems) {
            displayMenuItem(item);
        }
    }

    private void displayMenuItem(MenuItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menuItem.fxml"));
            VBox menuCard = loader.load();

            ImageView imageView = (ImageView) menuCard.lookup("#menuImage");
            Label nameLabel = (Label) menuCard.lookup("#menuName");
            Label priceLabel = (Label) menuCard.lookup("#menuPrice");
            Spinner<?> spinnerNode = (Spinner<?>) menuCard.lookup("#addToOrder");
            Button submitBtn = (Button) menuCard.lookup("#submitQuantityBtn");

            if (spinnerNode instanceof Spinner) {
                Spinner<Integer> quantitySpinner = (Spinner<Integer>) spinnerNode;

                // Configure spinner with proper bounds and start from 1
                SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
                quantitySpinner.setValueFactory(valueFactory);
                quantitySpinner.setEditable(false);
                menuSpinners.put(item.getId(), quantitySpinner);

                // Restore quantity if item is in cart
                CartItem cartItem = cartItems.get(item.getId());
                if (cartItem != null) {
                    quantitySpinner.getValueFactory().setValue(cartItem.getQuantity());
                }

                // Add to cart action
                submitBtn.setOnAction(e -> {
                    int quantity = quantitySpinner.getValue();
                    if (quantity > 0) {
                        addToCart(item, quantity);
                    }
                });
            }

            //  try to load the image
            String imagePath = item.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    //  bin/images path first
                    java.io.File file = new java.io.File("bin/" + imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        //  absolute path
                        file = new java.io.File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                        } else {
                            // Use Redis image as fallback
                            Image redisImage = new Image(getClass().getResourceAsStream("/images/error.png"));
                            imageView.setImage(redisImage);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + imagePath + " - " + e.getMessage());
                    // Use Redis image as fallback
                    Image redisImage = new Image(getClass().getResourceAsStream("/images/error.png"));
                    imageView.setImage(redisImage);
                }
            } else {
                // Use Redis image as fallback
                Image redisImage = new Image(getClass().getResourceAsStream("/images/error.png"));
                imageView.setImage(redisImage);
            }

            nameLabel.setText(item.getName());
            priceLabel.setText(String.format("%.2f DA", item.getPrice()));

            menuCard.setOnMouseClicked(event -> {
                currentCategory = item.getCategory();
                filterAndDisplayMenuItems();
                setupCategoryButtons(); // Update category buttons to reflect the selection
            });

            menuItemsContainer.getChildren().add(menuCard);

        } catch (IOException e) {
            System.err.println("Error loading menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //check this

   
    // Method to load menu items from the database
    private void loadMenuItems() {
        MenuItemDAO menuItemDAO = new MenuItemDAO();
        try {
            // Load menu items from database
            menuItems = menuItemDAO.getAllMenuItems();

        } catch (Exception e) {
            System.err.println("Error loading menu items from database: " + e.getMessage());
            e.printStackTrace();

        }
    }

    private void addToCart(MenuItem item, int quantity) {
        CartItem cartItem = cartItems.get(item.getId());
        if (cartItem == null) {
            cartItem = new CartItem(item, quantity);
            cartItems.put(item.getId(), cartItem);
        } else {
            cartItem.setQuantity(quantity);
        }

        updateCartDisplay();
        updateCartSummary();
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        for (CartItem cartItem : cartItems.values()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("cart_item.fxml"));
                VBox cartItemBox = loader.load();

                MenuItem item = cartItem.getMenuItem();

                Label foodNameLabel = (Label) cartItemBox.lookup("#foodNameLabel");
                Label priceLabel = (Label) cartItemBox.lookup("#priceLabel");
                Label quantityLabel = (Label) cartItemBox.lookup("#quantityLabel");
                Button plusBtn = (Button) cartItemBox.lookup("#plusBtn");
                Button minusBtn = (Button) cartItemBox.lookup("#minusBtn");
                Button deleteButton = (Button) cartItemBox.lookup("#deleteButton");

                foodNameLabel.setText(item.getName());
                priceLabel.setText(String.format("%.2f DA", cartItem.getTotal()));
                quantityLabel.setText(String.valueOf(cartItem.getQuantity()));

                Spinner<Integer> menuSpinner = menuSpinners.get(item.getId());

                plusBtn.setOnAction(e -> {
                    int newQuantity = cartItem.getQuantity() + 1;
                    if (newQuantity <= 10) {
                        cartItem.setQuantity(newQuantity);
                        if (menuSpinner != null) {
                            menuSpinner.getValueFactory().setValue(newQuantity);
                        }
                        updateCartDisplay();
                        updateCartSummary();
                    }
                });

                minusBtn.setOnAction(e -> {
                    if (cartItem.getQuantity() > 1) {
                        int newQuantity = cartItem.getQuantity() - 1;
                        cartItem.setQuantity(newQuantity);
                        if (menuSpinner != null) {
                            menuSpinner.getValueFactory().setValue(newQuantity);
                        }
                        updateCartDisplay();
                        updateCartSummary();
                    }
                });

                deleteButton.setOnAction(e -> {
                    cartItems.remove(item.getId());
                    if (menuSpinner != null) {
                        menuSpinner.getValueFactory().setValue(0);
                    }
                    updateCartDisplay();
                    updateCartSummary();
                });

                cartItemsContainer.getChildren().add(cartItemBox);

            } catch (IOException e) {
                System.out.println("Error loading cart item: " + e.getMessage());
            }
        }
    }

    private void updateCartSummary() {
        total = cartItems.values().stream()
                .mapToDouble(CartItem::getTotal)
                .sum();


        totalLabel.setText(String.format("%.2f DA", total));
    }

    @FXML
    void cancelOrder(@SuppressWarnings("unused") ActionEvent event) {
        cartItems.clear();
        menuSpinners.values().forEach(spinner -> spinner.getValueFactory().setValue(0));
        updateCartDisplay();
        updateCartSummary();
    }

    @FXML
    void placeOrder(ActionEvent event) {
        handlePlaceOrder();
    }

    @FXML
    void goToAdminLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) placeOrderBtn.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Admin Login");
            StageManager.applyStageSettings(stage);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading admin login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToWelcomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome.fxml"));
            Parent root = loader.load();

            Font customFont = Font.loadFont(
                getClass().getResourceAsStream("./tommy-mid.otf"),
                20
            );

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("menu-style.css").toExternalForm()); // Add the stylesheet

            Stage stage = (Stage) placeOrderBtn.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Welcome Page");
            StageManager.applyStageSettings(stage);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading admin login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePlaceOrder() {
        if (cartItems.isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Please add items before placing an order.");
            return;
        }

        Order order = new Order(new ArrayList<>(cartItems.values()), total);
        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.createOrder(order);

        String orderDetails = new String();
        orderDetails = orderDetails + ("Your order has been placed successfully.\n\n");
        orderDetails =orderDetails +("Order ID: ") + (orderId) +("\n") ;
        orderDetails =orderDetails +("Total Items: ")  + (cartItems.size()) +("\n");
        orderDetails = orderDetails +("Total Amount: ") +(String.format("%.2f", total)) + ("\n\n");

        if (orderId > 0) {
            showAlert("Order Placed", orderDetails);
            cartItems.clear();
            menuSpinners.values().forEach(spinner -> spinner.getValueFactory().setValue(0));
            updateCartDisplay();
            updateCartSummary();
        } else {
            showAlert("Error", "Failed to place order. Please try again.");
        }
    }

    private double getTotal() {
        return cartItems.values().stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
