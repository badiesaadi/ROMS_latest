import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private int orderId;
    private OrderStatus status;
    private Date date;
    private int customerId;
    private String staffId;
    private int kitchenId;
    private String managerId;
    private List<OrderItem> items;

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

        public int getQuantity() {
            return quantity;
        }
    }

    public Order(List<CartItem> cartItems, double total) {
        this.items = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            this.items.add(new OrderItem(cartItem.getMenuItem(), cartItem.getQuantity()));
        }
        this.status = OrderStatus.QUEUED;
        this.date = new Date(System.currentTimeMillis());
        this.customerId = 0;
        this.staffId = null;
        this.kitchenId = 0;
        this.managerId = null;
    }

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.QUEUED;
        this.date = new Date(System.currentTimeMillis());
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

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }

    public String getManagerId() {
        return managerId != null ? managerId : "N/A";
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
    }

    public String getItemsSummary() {
        return items.stream()
                .map(item -> item.getMenuItem().getTitle() + " x" + item.getQuantity())
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(orderId);
    }
}