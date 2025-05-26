 

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for viewing database contents
 */
public class DatabaseViewer {
    
    /**
     * Get a list of all tables in the database
     */
    public static List<String> getAllTables() {
        List<String> tables = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting tables: " + e.getMessage());
        }
        
        return tables;
    }
    
    /**
     * Get all data from a table as a formatted string
     */
    public static String getTableData(String tableName) {
        StringBuilder result = new StringBuilder();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                result.append(String.format("%-20s", metaData.getColumnName(i)));
            }
            result.append("\n");
            
            // Print separator
            for (int i = 1; i <= columnCount; i++) {
                result.append("--------------------");
            }
            result.append("\n");
            
            // Print data
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.append(String.format("%-20s", rs.getString(i)));
                }
                result.append("\n");
            }
            
        } catch (SQLException e) {
            result.append("Error getting table data: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) {
        System.out.println("Database Tables:");
        List<String> tables = getAllTables();
        for (String table : tables) {
            System.out.println("- " + table);
        }
        
        System.out.println("\nSample data from MenuItem table:");
        System.out.println(getTableData("MenuItem"));
        
        System.out.println("Sample data from Category table:");
        System.out.println(getTableData("Category"));
    }
} 