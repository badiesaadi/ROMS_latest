import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection connection;

    public OrderDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
            validateConnection();
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize OrderDAO", e);
        }
    }

    private void validateConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Connection is invalid, attempting to reconnect...");
            this.connection = DatabaseConnection.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Failed to re-establish database connection");
            }
            System.out.println("Connection re-established successfully.");
        }
    }

    public int insertOrder(Order order) {
        String sql = "INSERT INTO `order` (status, date) VALUES (?, ?)";

        try {
            validateConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getStatus().toString());
                pstmt.setDate(2, new Date(System.currentTimeMillis()));

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);

                            if (insertOrderItems(connection, orderId, order.getItems())) {
                                connection.commit();
                                System.out.println("Inserted order with ID: " + orderId);
                                return orderId;
                            } else {
                                connection.rollback();
                                return -1;
                            }
                        }
                    }
                }
                connection.rollback();
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private boolean insertOrderItems(Connection conn, int orderId, List<CartItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean updateOrderStatus(int orderId, Order.OrderStatus status) {
        String sql = "UPDATE `order` SET status = ? WHERE order_id = ?";

        try {
            validateConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status.toString());
                pstmt.setInt(2, orderId);

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Updated " + affectedRows + " rows for order ID " + orderId + " to status " + status);
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";

        try {
            validateConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, orderId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Order order = new Order();
                        order.setOrderId(rs.getInt("order_id"));
                        order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                        order.setDate(new java.sql.Date(rs.getTimestamp("date").getTime()));
                        order.setItems(getOrderItems(connection, orderId));
                        System.out.println("Retrieved order ID " + orderId + ": " + order);
                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order: " + e.getMessage());
        }
        System.out.println("No order found for ID " + orderId);
        return null;
    }

    private List<CartItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT m.item_id, m.title, m.price, m.category_title, m.image_path, om.quantity " +
                "FROM order_items om " +
                "JOIN menuitem m ON om.item_id = m.item_id " +
                "WHERE om.order_id = ?";
        System.out.println("Fetching items for orderId: " + orderId);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                int itemCount = 0;
                while (rs.next()) {
                    itemCount++;
                    MenuItem menuItem = new MenuItem(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getDouble("price"),
                            rs.getString("category_title"),
                            rs.getString("image_path")
                    );
                    items.add(new CartItem(menuItem, rs.getInt("quantity")));
                }
                System.out.println("Found " + itemCount + " items for orderId: " + orderId);
                return items;
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order`";

        try {
            validateConnection();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("Executing getAllOrders query...");
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Order order = new Order();
                    order.setOrderId(orderId);
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    order.setDate(rs.getDate("date"));
                    order.setItems(getOrderItems(connection, orderId));
                    orders.add(order);
                }
                System.out.println("getAllOrders returned " + orders.size() + " orders: " + orders);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        if (status == null || status == Order.OrderStatus.ALL) {
            return getAllOrders(); // Fallback to all orders if status is invalid or ALL
        }

        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `order` WHERE status = ?";
        System.out.println("Executing getOrdersByStatus with status: " + status.name());
        try {
            validateConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, status.name());
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Query executed, fetching results...");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        Order order = new Order();
                        order.setOrderId(rs.getInt("order_id"));
                        order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                        order.setDate(new java.sql.Date(rs.getTimestamp("date").getTime()));
                        order.setItems(getOrderItems(connection, order.getOrderId()));
                        orders.add(order);
                    }
                    System.out.println("Found " + count + " orders with status " + status.name());
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getOrdersByStatus: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO `order` (status, date) VALUES (?, ?)";

        try {
            validateConnection();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                conn.setAutoCommit(false);

                pstmt.setString(1, order.getStatus().name());
                pstmt.setTimestamp(2, new java.sql.Timestamp(order.getDate().getTime()));

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return -1;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        order.setOrderId(orderId);

                        if (insertOrderItemsBatch(conn, orderId, order.getItems())) {
                            conn.commit();
                            System.out.println("Created order with ID: " + orderId);
                            return orderId;
                        } else {
                            conn.rollback();
                            return -1;
                        }
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return -1;
        }
    }

    private boolean insertOrderItemsBatch(Connection conn, int orderId, List<CartItem> items)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;
        }
    }
}