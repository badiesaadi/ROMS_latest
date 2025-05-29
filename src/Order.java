import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class Order {
    public enum OrderStatus {
        QUEUED("Queued"),
        IN_PROGRESS("In Progress"),
        READY("Ready"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private int orderId;
    private OrderStatus status;
    private Date date;
    private int customerId;
    private int staffId;
    private List<OrderItem> items;
    private int kitchenId;
    private String managerId;

    public Order() {
        this.date = new Date(System.currentTimeMillis());
        this.items = new ArrayList<>();
        this.status = OrderStatus.QUEUED;
    }

    public Order(List<CartItem> cartItems, double total) {
        this();
        for (CartItem cartItem : cartItems) {
            this.items.add(new OrderItem(cartItem.getMenuItem(), cartItem.getQuantity()));
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }


    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
    }

    public String getItemsSummary() {
        return items.stream()
                .map(item -> String.format("%s x%d", item.getMenuItem().getTitle(), item.getQuantity()))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    public static class OrderItem {
        private MenuItem menuItem;
        private int quantity;

        public OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}

class OrderItem {
    private int orderId;
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(int orderId, MenuItem menuItem, int quantity) {
        this.orderId = orderId;
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public int getOrderId() {
        return orderId;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}