# ROMS Database Setup Instructions

Follow these steps to set up the database for the Restaurant Operations Management System.

## Prerequisites

- MySQL 8.0 or higher
- MySQL Workbench (optional, for visual database management)

## Setup Steps

### 1. Create the Database Schema

Run the `restaurant_schema.sql` script in your MySQL environment:

```bash
mysql -u root -p < restaurant_schema.sql
```

Alternatively, you can execute the SQL script using MySQL Workbench by:

1. Opening MySQL Workbench
2. Connecting to your MySQL server
3. Going to File > Open SQL Script
4. Selecting the `restaurant_schema.sql` file
5. Executing the script (lightning bolt icon)

### 2. Configure the Database Connection

Open the `DatabaseConnection.java` file and update the connection parameters:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root"; // Set your database password here
```

Replace the values with your MySQL server information:

- `DB_URL` - The JDBC URL for your MySQL server
- `DB_USER` - Your MySQL username
- `DB_PASSWORD` - Your MySQL password

### 3. Initial Data Setup (Optional)

You can populate your database with sample data by running the application. The Admin Dashboard allows you to add menu items which will be stored in the database.

If the application cannot connect to the database, it will fall back to using sample data which will not be persisted.

## Database Schema Overview

The ROMS database consists of the following key tables:

- `Manager` - Restaurant management staff
- `Kitchen` - Kitchen areas within the restaurant
- `Category` - Menu item categories
- `MenuItem` - The menu items available for order
- `Ingredient` - Ingredients used in menu items
- `Customer` - Customer information
- `Order` - Order details and status
- `Inventory` - Stock levels of ingredients
- `Supplier` - Supplier information
- `Purchase_Order` - Ingredient purchase orders

## Troubleshooting

If you encounter connection issues:

1. Verify MySQL is running:

   ```bash
   systemctl status mysql  # For Linux
   brew services list      # For macOS
   ```

2. Check the connection parameters in `DatabaseConnection.java`

3. Ensure your MySQL user has the necessary permissions:

   ```sql
   GRANT ALL PRIVILEGES ON restaurant_db.* TO 'your_username'@'localhost';
   FLUSH PRIVILEGES;
   ```

4. Test the connection independently:
   ```bash
   mysql -u your_username -p -e "USE restaurant_db; SHOW TABLES;"
   ```
