import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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

    public boolean updateMenuItem(MenuItem item) throws SQLException {
        if (!menuItemExists(item.getItemId())) {
            System.err.println("Menu item with ID " + item.getItemId() + " does not exist.");
            return false;
        }

        if (item.getCategoryTitle() != null && !categoryExists(item.getCategoryTitle())) {
            System.err.println("Category " + item.getCategoryTitle() + " does not exist.");
            return false;
        }

        String sql = "UPDATE MenuItem SET title = ?, price = ?, category_title = ?, image_path = ? WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategoryTitle());
            pstmt.setString(4, item.getImagePath());
            pstmt.setInt(5, item.getItemId());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Update MenuItem - Rows affected: " + affectedRows + ", Item ID: " + item.getItemId());
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            throw e;
        }
    }

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

    public int insertMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItem (title, price, category_title, image_path) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategoryTitle());
            pstmt.setString(4, item.getImagePath());

            

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
                    item.setCategoryTitle(rs.getString("category_title"));
                    item.setImagePath(rs.getString("image_path"));
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
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
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
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
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
        String query = "INSERT INTO MenuItem (title, price, category_title, image_path) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getTitle());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategoryTitle());
            pstmt.setString(4, item.getImagePath());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}