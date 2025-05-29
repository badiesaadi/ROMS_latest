import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

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
     * Insert a new menu item into the database.
     *
     * @param item The menu item to insert
     * @return The generated item ID if successful, -1 otherwise
     */
    public int insertMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItem (title, price, quantity, category_title, image_path, kitchen_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategoryTitle());
            pstmt.setString(5, item.getImagePath());

            // Check if kitchen_id exists in Staff table
            if (item.getKitchenId() != 0) {
                String checkKitchenSql = "SELECT COUNT(*) FROM Staff WHERE kitchen_id = ? AND role = 'kitchen_staff'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkKitchenSql)) {
                    checkStmt.setInt(1, item.getKitchenId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        // If kitchen_id doesn't exist, set it to null
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
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Update an existing menu item in the database.
     *
     * @param item The menu item to update
     * @return true if successful, false otherwise
     */
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE MenuItem SET title = ?, price = ?, quantity = ?, category_title = ?, image_path = ?, kitchen_id = ? WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategoryTitle());
            pstmt.setString(5, item.getImagePath());
            pstmt.setInt(6, item.getKitchenId());
            pstmt.setInt(7, item.getItemId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a menu item from the database.
     *
     * @param itemId The ID of the menu item to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMenuItem(int itemId) {
        String sql = "DELETE FROM MenuItem WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a menu item by its ID.
     *
     * @param itemId The ID of the menu item to retrieve
     * @return The menu item if found, null otherwise
     */
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
                    item.setCategoryTitle(rs.getString("category_title"));
                    item.setImagePath(rs.getString("image_path"));
                    item.setKitchenId(rs.getInt("kitchen_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu item: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all menu items from the database.
     *
     * @return A list of all menu items
     */
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
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Get all menu items in a specific category.
     *
     * @param category The category to filter by
     * @return A list of menu items in the specified category
     */
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
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Get a list of all categories from the database.
     *
     * @return A list of all categories
     */
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
            e.printStackTrace();
        }
        return items;
    }
}