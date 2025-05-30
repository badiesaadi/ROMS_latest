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

    // New button for navigation
    @FXML
    private Button backToCustomerViewButton;

    // Data
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private Order selectedOrder;
    private OrderDAO orderDAO;

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

    private void setupStatusComboBox() {
        statusComboBox.getItems().clear(); // Clear existing items
        statusComboBox.getItems().addAll(Order.OrderStatus.ALL, Order.OrderStatus.QUEUED, Order.OrderStatus.IN_PROGRESS, Order.OrderStatus.READY); // Add "All" and other statuses
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
            items1.add(new CartItem(new MenuItem(1, "Cappuccino", 4.95, "Coffee", ""), 2));
            items1.add(new CartItem(new MenuItem(2, "Mushroom Pizza", 9.95, "Italian", ""), 1));

            List<CartItem> items2 = new ArrayList<>();
            items2.add(new CartItem(new MenuItem(4, "Meat burger", 5.95, "Burger", ""), 3));
            items2.add(new CartItem(new MenuItem(5, "Fresh melon juice", 3.95, "Drinks", ""), 3));

            Order order1 = new Order(items1, 19.85);
            Order order2 = new Order(items2, 29.7);

            orders.add(order1);
            orders.add(order2);
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

    private void handleOrderItemClick(CartItem cartItem) {
        try {
            MenuItem menuItem = cartItem.getMenuItem();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Item Details");
            alert.setHeaderText(menuItem.getTitle());
            alert.setContentText(String.format(
                    "Price: %.2f\nQuantity: %d\nCategory: %s\nKitchen: %d",
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
            List<Order> orders = orderDAO.getOrdersByStatus(Order.OrderStatus.QUEUED);
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.IN_PROGRESS));
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.READY));
            System.out.println("Refreshing orders table with " + orders.size() + " orders");
            this.orders.setAll(orders);
            ordersTable.setItems(this.orders);
            ordersTable.refresh();
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
                List<Order> filteredOrders;
                if (selectedStatus == Order.OrderStatus.ALL) {
                    filteredOrders = orderDAO.getAllOrders(); // Fetch all orders when "All" is selected
                } else {
                    filteredOrders = orderDAO.getOrdersByStatus(selectedStatus);
                }
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
}