import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, String> orderItemsColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private TableColumn<Order, String> totalColumn;
    @FXML
    private TableColumn<Order, Void> actionsColumn;

    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private OrderDAO orderDAO = new OrderDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOrdersTable();
        loadOrders();
        startOrderRefreshThread();
    }

    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        orderDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        orderItemsColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getItemsSummary()));
        statusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        totalColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));

        actionsColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pane = new HBox(cancelBtn);

            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                cancelBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    updateOrderStatus(order, Order.OrderStatus.CANCELLED);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        ordersTable.setItems(orders);
    }

    private void loadOrders() {
        List<Order> existingOrders = orderDAO.getAllOrders();
        orders.addAll(existingOrders);
    }

    private void startOrderRefreshThread() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    Platform.runLater(() -> {
                        List<Order> currentOrders = orderDAO.getOrdersByStatus(Order.OrderStatus.QUEUED);
                        currentOrders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.IN_PROGRESS));
                        currentOrders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.READY));
                        orders.clear();
                        orders.addAll(currentOrders);
                        ordersTable.refresh();
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    private void updateOrderStatus(Order order, Order.OrderStatus newStatus) {
        order.setStatus(newStatus);
        orderDAO.updateOrderStatus(order.getOrderId(), newStatus);
        ordersTable.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Order status updated to " + newStatus.getDisplayName());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}