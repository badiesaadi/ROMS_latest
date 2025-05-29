import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class KitchenDashboardController implements Initializable {

    // Orders Tab
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, String> orderItemsColumn;
    @FXML
    private TableColumn<Order, String> deliveryPartnerColumn;
    @FXML
    private TableColumn<Order, Order.OrderStatus> statusColumn;
    @FXML
    private TableColumn<Order, Void> actionsColumn;

    // Order Details
    @FXML
    private Label selectedOrderIdLabel;
    @FXML
    private ComboBox<Order.OrderStatus> statusComboBox;
    @FXML
    private Button updateOrderButton;

    // Inventory Tab
    @FXML
    private TableView<Ingredient> inventoryTable;
    @FXML
    private TableColumn<Ingredient, Integer> ingredientIdColumn;
    @FXML
    private TableColumn<Ingredient, String> ingredientNameColumn;
    @FXML
    private TableColumn<Ingredient, Double> quantityColumn;
    @FXML
    private TableColumn<Ingredient, String> unitColumn;
    @FXML
    private TableColumn<Ingredient, Double> minQuantityColumn;
    @FXML
    private TableColumn<Ingredient, String> categoryColumn;
    @FXML
    private TableColumn<Ingredient, Void> inventoryActionsColumn;

    // Inventory Form
    @FXML
    private TextField ingredientNameField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField minQuantityField;
    @FXML
    private TextField unitField;
    @FXML
    private ComboBox<String> ingredientCategoryComboBox;
    @FXML
    private Button addIngredientButton;
    @FXML
    private Button updateIngredientButton;
    @FXML
    private Button clearIngredientFormButton;

    // Low Stock Notifications
    @FXML
    private VBox lowStockContainer;

    // Data
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
    private List<MenuItemIngredient> menuItemIngredients = new ArrayList<>();
    private Order selectedOrder;
    private Ingredient selectedIngredient;
    private int nextIngredientId = 1;
    private OrderDAO orderDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            orderDAO = new OrderDAO();
            setupOrdersTable();
            setupInventoryTable();
            setupStatusComboBox();
            setupIngredientCategoryComboBox();
            loadOrders();
            loadSampleIngredients();
            setupIngredientMappings();
            checkLowStockIngredients();

            // Start a thread to periodically refresh orders
            startOrderRefreshThread();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Initialization Error", "Failed to initialize dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        orderDateColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        orderItemsColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemsSummary()));
        deliveryPartnerColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManagerId()));
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));

        // Setup actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button actionBtn = new Button("Update Status");

            {
                actionBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleOrderStatusChange(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Order order = getTableRow().getItem();
                    actionBtn.setDisable(order.getStatus() == Order.OrderStatus.READY); // Disable button if status is READY
                    setGraphic(actionBtn);
                }
            }
        });

        refreshOrdersTable();
    }

    private void setupInventoryTable() {
        ingredientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        minQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Custom cell factory for quantity column to highlight low stock
        quantityColumn.setCellFactory(column -> new TableCell<Ingredient, Double>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", quantity));

                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Ingredient ingredient = (Ingredient) getTableRow().getItem();
                        if (ingredient.isLowStock()) {
                            setStyle("-fx-text-fill: #e74c3c;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            }
        });

        // Setup actions column with buttons for inventory
        inventoryActionsColumn.setCellFactory(param -> new TableCell<Ingredient, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button addStockBtn = new Button("+");
            private final HBox pane = new HBox(5, editBtn, addStockBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                addStockBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    populateIngredientForm(ingredient);
                });

                addStockBtn.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    showAddStockDialog(ingredient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Handle row selection
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedIngredient = newSelection;
                populateIngredientForm(selectedIngredient);
            }
        });
    }

    private void setupStatusComboBox() {
        statusComboBox.getItems().clear(); // Clear existing items
        statusComboBox.getItems().addAll(Order.OrderStatus.QUEUED, Order.OrderStatus.IN_PROGRESS, Order.OrderStatus.READY); // Add only the desired statuses
    }

    private void setupIngredientCategoryComboBox() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Meat", "Dairy", "Produce", "Bakery", "Spices", "Beverages", "Dry Goods", "Other");
        ingredientCategoryComboBox.setItems(categories);
    }

    private void loadOrders() {
        try {
            // Get orders from the database
            List<Order> dbOrders = orderDAO.getOrdersByStatus(Order.OrderStatus.QUEUED);
            dbOrders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.IN_PROGRESS));
            orders.addAll(dbOrders);

            // If no orders exist, create sample orders
            if (orders.isEmpty()) {
                createSampleOrders();
            }

            ordersTable.setItems(orders);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Load Orders Error", "Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleOrders() {
        try {
            List<CartItem> items1 = new ArrayList<>();
            items1.add(new CartItem(new MenuItem(1, "Cappuccino", 4.95, "Coffee", "", 1), 2));
            items1.add(new CartItem(new MenuItem(2, "Mushroom Pizza", 9.95, "Italian", "", 1), 1));

            List<CartItem> items2 = new ArrayList<>();
            items2.add(new CartItem(new MenuItem(4, "Meat burger", 5.95, "Burger", "", 1), 3));
            items2.add(new CartItem(new MenuItem(5, "Fresh melon juice", 3.95, "Drinks", "", 1), 3));

            Order order1 = new Order(items1, 19.85);
            Order order2 = new Order(items2, 29.7);

            orders.add(order1);
            orders.add(order2);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Sample Orders Error", "Failed to create sample orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSampleIngredients() {
        try {
            ingredients.add(new Ingredient(nextIngredientId++, "Flour", 10.0, 5.0, "kg", "Bakery"));
            ingredients.add(new Ingredient(nextIngredientId++, "Tomatoes", 5.0, 2.0, "kg", "Produce"));
            ingredients.add(new Ingredient(nextIngredientId++, "Ground Beef", 8.0, 3.0, "kg", "Meat"));
            ingredients.add(new Ingredient(nextIngredientId++, "Mozzarella Cheese", 4.0, 1.5, "kg", "Dairy"));
            ingredients.add(new Ingredient(nextIngredientId++, "Coffee Beans", 3.0, 1.0, "kg", "Beverages"));
            ingredients.add(new Ingredient(nextIngredientId++, "Lettuce", 1.5, 1.0, "kg", "Produce"));
            ingredients.add(new Ingredient(nextIngredientId++, "Chicken Breast", 6.0, 2.0, "kg", "Meat"));
            ingredients.add(new Ingredient(nextIngredientId++, "Burger Buns", 40.0, 10.0, "pcs", "Bakery"));
            ingredients.add(new Ingredient(nextIngredientId++, "Milk", 7.0, 2.0, "L", "Dairy"));
            ingredients.add(new Ingredient(nextIngredientId++, "Mushrooms", 0.5, 1.0, "kg", "Produce"));

            inventoryTable.setItems(ingredients);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Sample Ingredients Error", "Failed to load sample ingredients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupIngredientMappings() {
        try {
            for (Ingredient ingredient : ingredients) {
                for (Order order : orders) {
                    for (Order.OrderItem orderItem : order.getItems()) {
                        MenuItem menuItem = orderItem.getMenuItem();
                        if (menuItem.getTitle().toLowerCase().contains(ingredient.getName().toLowerCase())) {
                            menuItemIngredients.add(new MenuItemIngredient(menuItem, ingredient, 0.1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Ingredient Mapping Error", "Failed to setup ingredient mappings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkLowStockIngredients() {
        try {
            lowStockContainer.getChildren().clear();
            List<Ingredient> lowStock = ingredients.stream()
                    .filter(Ingredient::isLowStock)
                    .collect(Collectors.toList());

            if (lowStock.isEmpty()) {
                Label noLowStockLabel = new Label("No low stock ingredients");
                noLowStockLabel.setStyle("-fx-text-fill: #2ecc71;");
                lowStockContainer.getChildren().add(noLowStockLabel);
                return;
            }

            for (Ingredient ingredient : lowStock) {
                Label lowStockLabel = new Label(ingredient.getName() + " is low in stock! " +
                        String.format("%.2f%s remaining (minimum: %.2f%s)",
                                ingredient.getQuantity(),
                                ingredient.getUnit(),
                                ingredient.getMinQuantity(),
                                ingredient.getUnit()));
                lowStockLabel.setStyle("-fx-text-fill: #e74c3c;");
                lowStockLabel.setWrapText(true);
                lowStockContainer.getChildren().add(lowStockLabel);
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Low Stock Check Error", "Failed to check low stock ingredients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateOrderDetails(Order order) {
        try {
            selectedOrderIdLabel.setText("Order: ");
            statusComboBox.setValue(order.getStatus());
            updateOrderButton.setDisable(false);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Populate Order Error", "Failed to populate order details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateIngredientForm(Ingredient ingredient) {
        try {
            ingredientNameField.setText(ingredient.getName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            minQuantityField.setText(String.valueOf(ingredient.getMinQuantity()));
            unitField.setText(ingredient.getUnit());
            ingredientCategoryComboBox.setValue(ingredient.getCategory());
            addIngredientButton.setDisable(true);
            updateIngredientButton.setDisable(false);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Populate Ingredient Error", "Failed to populate ingredient form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddStockDialog(Ingredient ingredient) {
        try {
            TextInputDialog dialog = new TextInputDialog("1.0");
            dialog.setTitle("Add Stock");
            dialog.setHeaderText("Add stock to " + ingredient.getName());
            dialog.setContentText("Enter amount to add:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(amount -> {
                try {
                    double amountValue = Double.parseDouble(amount);
                    if (amountValue > 0) {
                        ingredient.increaseQuantity(amountValue);
                        inventoryTable.refresh();
                        checkLowStockIngredients();
                    }
                } catch (NumberFormatException e) {
                    showAlert(AlertType.ERROR, "Input Error", "Please enter a valid number.");
                }
            });
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Add Stock Error", "Failed to add stock: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startOrderRefreshThread() {
        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000);
                    Platform.runLater(this::refreshOrdersTable);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Refresh Error", "Failed to refresh orders: " + e.getMessage()));
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    @FXML
    private void handleUpdateOrder() {
        try {
            if (selectedOrder == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select an order to update.");
                return;
            }
            selectedOrder.setStatus(statusComboBox.getValue());
            orderDAO.updateOrderStatus(selectedOrder.getOrderId(), selectedOrder.getStatus());
            ordersTable.refresh();
            showAlert(AlertType.INFORMATION, "Success", "Order updated successfully.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Update Order Error", "Failed to update order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddIngredient() {
        try {
            String name = ingredientNameField.getText().trim();
            double quantity = Double.parseDouble(quantityField.getText().trim());
            double minQuantity = Double.parseDouble(minQuantityField.getText().trim());
            String unit = unitField.getText().trim();
            String category = ingredientCategoryComboBox.getValue();

            if (name.isEmpty() || unit.isEmpty() || category == null) {
                showAlert(AlertType.ERROR, "Input Error", "Please fill all fields.");
                return;
            }

            Ingredient newIngredient = new Ingredient(nextIngredientId++, name, quantity, minQuantity, unit, category);
            ingredients.add(newIngredient);
            clearIngredientForm();
            checkLowStockIngredients();
            showAlert(AlertType.INFORMATION, "Success", "Ingredient added successfully.");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity fields.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Add Ingredient Error", "Failed to add ingredient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateIngredient() {
        try {
            if (selectedIngredient == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select an ingredient to update.");
                return;
            }
            selectedIngredient.setName(ingredientNameField.getText().trim());
            selectedIngredient.setQuantity(Double.parseDouble(quantityField.getText().trim()));
            selectedIngredient.setMinQuantity(Double.parseDouble(minQuantityField.getText().trim()));
            selectedIngredient.setUnit(unitField.getText().trim());
            selectedIngredient.setCategory(ingredientCategoryComboBox.getValue());
            inventoryTable.refresh();
            clearIngredientForm();
            checkLowStockIngredients();
            showAlert(AlertType.INFORMATION, "Success", "Ingredient updated successfully.");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity fields.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Update Ingredient Error", "Failed to update ingredient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearIngredientForm() {
        try {
            clearIngredientForm();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Clear Form Error", "Failed to clear ingredient form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearIngredientForm() {
        ingredientNameField.clear();
        quantityField.clear();
        minQuantityField.clear();
        unitField.clear();
        ingredientCategoryComboBox.setValue(null);
        selectedIngredient = null;
        addIngredientButton.setDisable(false);
        updateIngredientButton.setDisable(true);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToCustomerView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer_view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateOrderStatus(Order order, Order.OrderStatus newStatus) {
        try {
            order.setStatus(newStatus);
            orderDAO.updateOrderStatus(order.getOrderId(), newStatus);
            refreshOrdersTable();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Update Status Error", "Failed to update order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOrderItemClick(Order.OrderItem orderItem) {
        try {
            MenuItem menuItem = orderItem.getMenuItem();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Item Details");
            alert.setHeaderText(menuItem.getTitle());
            alert.setContentText(String.format(
                    "Price: $%.2f\nQuantity: %d\nCategory: %s\nKitchen: %d",
                    menuItem.getPrice(),
                    orderItem.getQuantity(),
                    menuItem.getCategoryTitle(),
                    menuItem.getKitchenId()));
            alert.showAndWait();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Item Click Error", "Failed to display item details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshOrdersTable() {
        try {
            List<Order> orders = orderDAO.getOrdersByStatus(Order.OrderStatus.QUEUED);
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.IN_PROGRESS));
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.READY)); // Include READY orders
            this.orders.setAll(orders);
            ordersTable.setItems(this.orders);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Refresh Orders Error", "Failed to refresh orders table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOrderStatusChange(Order order) {
        try {
            switch (order.getStatus()) {
                case QUEUED:
                    updateOrderStatus(order, Order.OrderStatus.IN_PROGRESS);
                    break;
                case IN_PROGRESS:
                    updateOrderStatus(order, Order.OrderStatus.READY);
                    break;
                case READY:
                    updateOrderStatus(order, Order.OrderStatus.DELIVERED);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Status Change Error", "Failed to change order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStatusFilter() {
        Order.OrderStatus selectedStatus = statusComboBox.getValue();
        if (selectedStatus != null) {
            try {
                List<Order> filteredOrders = orderDAO.getOrdersByStatus(selectedStatus);
                this.orders.setAll(filteredOrders);
                ordersTable.setItems(this.orders);
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Filter Error", "Failed to filter orders: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            refreshOrdersTable(); // Show all orders if no status is selected
        }
    }

    // private void setupUserPermissions() {
    //     if (currentUser.getRole().equals("kitchen")) {
    //         statusComboBox.setDisable(false); // Allow filtering
    //         actionsColumn.setVisible(true); // Allow editing order status
    //     }
    // }
}