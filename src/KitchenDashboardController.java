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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
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

    // Navigation buttons
    @FXML
    private Button backToCustomerViewButton;
    @FXML
    private Button backToCustomerViewFromKitchenButton;

    // Data
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private Order selectedOrder;
    private OrderDAO orderDAO;
    private boolean isUpdating = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            orderDAO = new OrderDAO();
            setupOrdersTable();
            setupStatusComboBox();
            loadOrders();
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
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));
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
                    actionBtn.setDisable(order.getStatus() == Order.OrderStatus.READY);
                    setGraphic(actionBtn);
                }
            }
        });

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedOrder = newSelection;
            if (selectedOrder != null) {
                selectedOrderIdLabel.setText("Order ID: " + selectedOrder.getOrderId());
                statusComboBox.setValue(selectedOrder.getStatus());
            } else {
                selectedOrderIdLabel.setText("Order ID: None");
                statusComboBox.setValue(null);
            }
        });

        // Set a placeholder for when the table is empty
        ordersTable.setPlaceholder(new Label("No orders found for the selected status."));

        refreshOrdersTable();
    }

    private void setupStatusComboBox() {
        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll(Order.OrderStatus.ALL, Order.OrderStatus.QUEUED,
                Order.OrderStatus.IN_PROGRESS, Order.OrderStatus.READY);
        statusComboBox.setValue(Order.OrderStatus.ALL);
        statusComboBox.setOnAction(event -> handleStatusFilter());
    }

    private void loadOrders() {
        try {
            List<Order> dbOrders = orderDAO.getAllOrders();
            System.out.println("Loaded " + dbOrders.size() + " orders from database: " + dbOrders);
            orders.addAll(dbOrders);

            if (orders.isEmpty()) {
                createSampleOrders();
                System.out.println("No database orders found, using sample orders: " + orders);
            }

            ordersTable.setItems(orders);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Load Orders Error", "Failed to load orders: " + e.getMessage());
            e.printStackTrace();
            createSampleOrders(); // Fallback to sample orders on error
            System.out.println("Error loading orders, using sample orders: " + orders);
        }
    }

    private void createSampleOrders() {
        try {
            List<CartItem> items1 = new ArrayList<>();
            items1.add(new CartItem(new MenuItem(1, "Cappuccino", 4.95, "Coffee", ""), 2));
            items1.add(new CartItem(new MenuItem(2, "Mushroom Pizza", 9.95, "Italian", ""), 1));

            List<CartItem> items2 = new ArrayList<>();
            items2.add(new CartItem(new MenuItem(4, "Meat Burger", 5.95, "Burger", ""), 3));
            items2.add(new CartItem(new MenuItem(5, "Fresh Melon Juice", 3.95, "Drinks", ""), 3));

            Order order1 = new Order(items1, 19.85);
            Order order2 = new Order(items2, 29.7);
            order1.setOrderId(1); // Assign temporary IDs
            order2.setOrderId(2);
            order1.setDate(new java.sql.Date(System.currentTimeMillis()));
            order2.setDate(new java.sql.Date(System.currentTimeMillis()));
            order1.setStatus(Order.OrderStatus.QUEUED);
            order2.setStatus(Order.OrderStatus.IN_PROGRESS);

            orders.add(order1);
            orders.add(order2);
            System.out.println("Created sample orders: " + orders);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Sample Orders Error", "Failed to create sample orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startOrderRefreshThread() {
        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000);
                    if (!isUpdating) {
                        Platform.runLater(this::refreshOrdersTable);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Refresh Error",
                            "Failed to refresh orders: " + e.getMessage()));
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
            isUpdating = true;
            Order.OrderStatus newStatus = statusComboBox.getValue();
            if (newStatus == null || newStatus == Order.OrderStatus.ALL) {
                showAlert(AlertType.WARNING, "Invalid Status", "Please select a valid status.");
                return;
            }
            selectedOrder.setStatus(newStatus);
            if (orderDAO.updateOrderStatus(selectedOrder.getOrderId(), newStatus)) {
                System.out.println("Updated order " + selectedOrder.getOrderId() + " to status: " + newStatus);
                refreshOrdersTable();
                showAlert(AlertType.INFORMATION, "Success", "Order updated successfully.");
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update order status in database.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Update Order Error", "Failed to update order: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
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

    @FXML
    private void handleBackToCustomerViewFromKitchen() {
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
            isUpdating = true;
            order.setStatus(newStatus);
            if (orderDAO.updateOrderStatus(order.getOrderId(), newStatus)) {
                System.out.println("Updated order " + order.getOrderId() + " to status: " + newStatus);
                refreshOrdersTable();
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update order status in database.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Update Status Error", "Failed to update order status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }

    private void handleOrderItemClick(CartItem cartItem) {
        try {
            MenuItem menuItem = cartItem.getMenuItem();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Item Details");
            alert.setHeaderText(menuItem.getTitle());
            alert.setContentText(String.format(
                    "Price: $%.2f\nQuantity: %d\nCategory: %s",
                    menuItem.getPrice(),
                    cartItem.getQuantity(),
                    menuItem.getCategoryTitle()));
            alert.showAndWait();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Item Click Error", "Failed to display item details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshOrdersTable() {
        try {
            List<Order> dbOrders = new ArrayList<>();
            Order.OrderStatus filterStatus = statusComboBox.getValue();

            if (filterStatus == null || filterStatus == Order.OrderStatus.ALL) {
                dbOrders.addAll(orderDAO.getAllOrders());
                System.out.println("Fetched all orders: " + dbOrders.size() + " orders - " + dbOrders);
            } else {
                dbOrders.addAll(orderDAO.getOrdersByStatus(filterStatus));
                System.out.println("Fetched orders by status " + filterStatus + ": " + dbOrders.size() + " orders - " + dbOrders);
            }

            // Clear and set only the filtered orders
            orders.clear();
            orders.addAll(dbOrders);
            System.out.println("After filter, table has " + orders.size() + " orders: " + orders);
            ordersTable.setItems(orders);
            ordersTable.refresh();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Refresh Orders Error", "Failed to refresh orders table: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Refresh failed, retaining current orders: " + orders);
            ordersTable.refresh();
        }
    }

    private void handleOrderStatusChange(Order order) {
        try {
            Order.OrderStatus newStatus = null;
            switch (order.getStatus()) {
                case QUEUED:
                    newStatus = Order.OrderStatus.IN_PROGRESS;
                    break;
                case IN_PROGRESS:
                    newStatus = Order.OrderStatus.READY;
                    break;
                default:
                    return;
            }
            updateOrderStatus(order, newStatus);

            Order.OrderStatus filterStatus = statusComboBox.getValue();
            if (filterStatus != null && filterStatus != Order.OrderStatus.ALL && filterStatus != newStatus) {
                showAlert(AlertType.INFORMATION, "Order Updated",
                        "Order status changed to " + newStatus + ". It may no longer appear due to the current filter.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Status Change Error", "Failed to change order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStatusFilter() {
        Order.OrderStatus selectedStatus = statusComboBox.getValue();
        System.out.println("Applying filter: " + selectedStatus);
        refreshOrdersTable();
    }
}