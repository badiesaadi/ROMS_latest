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
        ALL("All"),
        IN_PROGRESS("In Progress"),
        READY("Ready"),

        //chech this
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
    private List<CartItem> items;

    public Order() {
        this.date = new Date(System.currentTimeMillis());
        this.items = new ArrayList<>();
        this.status = OrderStatus.QUEUED;
    }

    public Order(List<CartItem> cartItems, double total) {
        this();
        this.items = new ArrayList<>(cartItems);
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


    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
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
}
