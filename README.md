# ROMS - Restaurant Order Management System

A comprehensive restaurant management system with a Java-based frontend and MySQL database for the backend.

## Features

- **Menu Management**: Organize and maintain your restaurant's menu items and categories
- **Order Processing**: Track and manage customer orders and their status
- **Kitchen Operations**: Manage kitchen workflows and menu item preparation
- **Admin Dashboard**: Complete control over restaurant operations
- **Customer Interface**: User-friendly ordering experience

## System Architecture

### Frontend

- Java-based application using JavaFX for the UI
- Custom CSS styling for a modern user experience

### Backend

- MySQL database with comprehensive schema
- Core entities: Menu Items, Categories, Orders
- Relational tables for many-to-many relationships

## Getting Started

1. Import the `restaurant_schema.sql` file into your MySQL environment
2. Configure database connection in your application
3. Navigate to the src folder and Run the application using the provided `run.bat` script
4. Start using the system to manage your restaurant operations

## User Interfaces

- **Admin Dashboard**: Complete restaurant management
- **Kitchen Dashboard**: Order management and preparation tracking
- **Customer View**: Menu browsing and ordering interface
- **Login System**: For accessing the admin and kitchen dashboards

## Prerequisites

- JDK 11 or higher
- JavaFX SDK 21.x
- MySQL 8.0 or higher
- MySQL Workbench (optional)

## Setup Steps

### 1. Create the Database Schema

Run the `restaurant_schema.sql` script in your MySQL environment in the terminal

Alternatively, you can execute the SQL script using MySQL workbench by:

1. Opening MySQL Workbench
2. Connecting to your MySQL server
3. Going to File > Open SQL Script
4. Selecting the `restaurant_schema.sql` file
5. Executing the script

### 2. Configure the Database Connection

Open the `DatabaseConnection.java` file and update the connection parameters:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root"; //  your database password goes here
```

Replace the values with your MySQL server information:

- `DB_URL` - the JDBC URL for your MySQL server
- `DB_USER` - the MySQL username
- `DB_PASSWORD` - the MySQL password

. Download MySQL Connector (JDBC Driver)
. Add the JAR file to your project's lib directory

### 3. Configure the Paths in run.bat

in run.bat file:

- Set the JavaFX path ( update this with your actual JavaFX path )

```bat
 set JAVAFX_PATH=C:\path\to\javafx-sdk-21.0.6
```

- Set the MySQL connector path

```bat
  set MYSQL_CONNECTOR=C:\path\to\mysql-connector-j-8.0.33.jar
```

## Troubleshooting

If you encounter connection issues:

1. Verify MySQL is running
2. Check the connection parameters in `DatabaseConnection.java`
3. Ensure your MySQL user has the necessary permissions
4. Test the connection independently
5. Verify your PATHS and installations
