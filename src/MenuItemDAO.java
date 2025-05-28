import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the MenuItem entity.
 * Handles database operations related to menu items.
 */
public class MenuItemDAO {

    private Connection connection;

    public MenuItemDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize MenuItemDAO", e);
        }
    }

    /**
     * Update an existing menu item in the database.
     *
     * @param item The menu item to update
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean updateMenuItem(MenuItem item) throws SQLException {
        // Validate item_id existence
        if (!menuItemExists(item.getItemId())) {
            System.err.println("Menu item with ID " + item.getItemId() + " does not exist.");
            return false;
        }

        // Validate category_title
        if (item.getCategoryTitle() != null && !categoryExists(item.getCategoryTitle())) {
            System.err.println("Category " + item.getCategoryTitle() + " does not exist.");
            return false;
        }

        String sql = "UPDATE MenuItem SET title = ?, price = ?, quantity = ?, category_title = ?, image_path = ?, kitchen_id = ? WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategoryTitle());
            pstmt.setString(5, item.getImagePath());

            // Validate kitchen_id
            if (item.getKitchenId() != 0) {
                String checkKitchenSql = "SELECT COUNT(*) FROM Staff WHERE kitchen_id = ? AND role = 'kitchen_staff'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkKitchenSql)) {
                    checkStmt.setInt(1, item.getKitchenId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        pstmt.setNull(6, Types.INTEGER);
                    } else {
                        pstmt.setInt(6, item.getKitchenId());
                    }
                }
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setInt(7, item.getItemId());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Update MenuItem - Rows affected: " + affectedRows + ", Item ID: " + item.getItemId());
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            throw e; // Prop propagate the exception to the caller
        }
    }

    /**
     * Check if a menu item exists by ID.
     *
     * @param itemId The ID to check
     * @return true if exists, false otherwise
     */
    private boolean menuItemExists(int itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MenuItem WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Check if a category exists by title.
     *
     * @param categoryTitle The category title to check
     * @return true if exists, false otherwise
     */
    private boolean categoryExists(String categoryTitle) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Category WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryTitle);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    // Rest of the methods remain unchanged
    public int insertMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItem (title, price, quantity, category_title, image_path, kitchen_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategoryTitle());
            pstmt.setString(5, item.getImagePath());

            if (item.getKitchenId() != 0) {
                String checkKitchenSql = "SELECT COUNT(*) FROM Staff WHERE kitchen_id = ? AND role = 'kitchen_staff'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkKitchenSql)) {
                    checkStmt.setInt(1, item.getKitchenId());
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        pstmt.setNull(6, Types.INTEGER);
                    } else {
                        pstmt.setInt(6, item.getKitchenId());
                    }
                }
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error inserting menu item: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteMenuItem(int itemId) {
        String sql = "DELETE FROM MenuItem WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public MenuItem getMenuItemById(int itemId) {
        String sql = "SELECT * FROM MenuItem WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    MenuItem item = new MenuItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setTitle(rs.getString("title"));
                    item.setPrice(rs.getDouble("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCategoryTitle(rs.getString("category_title"));
                    item.setImagePath(rs.getString("image_path"));
                    item.setKitchenId(rs.getInt("kitchen_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu item: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setItemId(rs.getInt("item_id"));
                item.setTitle(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public List<MenuItem> getMenuItemsByCategory(String categoryTitle) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem WHERE category_title = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryTitle);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setItemId(rs.getInt("item_id"));
                item.setTitle(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items by category: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT title FROM Category";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public void addMenuItem(MenuItem item) {
        String query = "INSERT INTO MenuItem (title, price, quantity, category_title, image_path, kitchen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategoryTitle());
            pstmt.setString(5, item.getImagePath());
            pstmt.setInt(6, item.getKitchenId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<MenuItem> getMenuItemsByKitchen(int kitchenId) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem WHERE kitchen_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, kitchenId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setItemId(rs.getInt("item_id"));
                item.setTitle(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items by kitchen: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
}