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
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize OrderDAO", e);
        }
    }

    public int insertOrder(Order order) {
        String sql = "INSERT INTO `order` (status, date, customer_id, staff_id) VALUES (?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getStatus().toString());
                pstmt.setDate(2, new Date(System.currentTimeMillis()));
                pstmt.setNull(3, java.sql.Types.INTEGER);
                pstmt.setNull(4, java.sql.Types.INTEGER);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            if (insertOrderItems(connection, orderId, order.getItems())) {
                                connection.commit();
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
                if (connection != null)
                    connection.rollback();
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

    private boolean insertOrderItems(Connection conn, int orderId, List<Order.OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Order.OrderItem item : items) {
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, orderId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    order.setDate(new java.sql.Date(rs.getTimestamp("date").getTime()));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStaffId(rs.getString("staff_id"));
                    order.setKitchenId(rs.getInt("kitchen_id"));
                    order.setManagerId(rs.getString("manager_id"));
                    order.setItems(getOrderItems(conn, orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order: " + e.getMessage());
        }
        return null;
    }

    private List<Order.OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT m.item_id, m.title, m.price, m.category_title, m.image_path, m.kitchen_id, om.quantity " +
                "FROM order_items om " +
                "JOIN menuitem m ON om.item_id = m.item_id " +
                "WHERE om.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Order.OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    MenuItem menuItem = new MenuItem(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getDouble("price"),
                            rs.getString("category_title"),
                            rs.getString("image_path"),
                            rs.getInt("kitchen_id"));
                    items.add(new Order.OrderItem(menuItem, rs.getInt("quantity")));
                }
                return items;
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order`";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Order order = new Order();
                order.setOrderId(orderId);
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getDate("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                if (rs.getObject("staff_id") != null) {
                    order.setStaffId(rs.getString("staff_id"));
                }
                order.setItems(getOrderItems(connection, orderId));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `order` WHERE status = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(new java.sql.Date(rs.getTimestamp("date").getTime()));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setItems(getOrderItems(connection, order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO `order` (status, date, customer_id, staff_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, order.getStatus().name());
            pstmt.setTimestamp(2, new java.sql.Timestamp(order.getDate().getTime()));
            pstmt.setNull(3, java.sql.Types.INTEGER);
            pstmt.setNull(4, java.sql.Types.INTEGER);
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
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return -1;
        }
    }

    private boolean insertOrderItemsBatch(Connection conn, int orderId, List<Order.OrderItem> items)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Order.OrderItem item : items) {
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

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `order` WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getDate("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setItems(getOrderItems(connection, order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByStaff(String staffId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `order` WHERE staff_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getDate("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setItems(getOrderItems(connection, order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}